package me.xleiten.coolgpsapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.OnClickListener
import android.view.View.VISIBLE
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationListenerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import me.xleiten.coolgpsapp.area.AreaType
import me.xleiten.coolgpsapp.database.Database
import me.xleiten.coolgpsapp.view.FigureView
import java.util.Date
import java.util.UUID

class MainActivity : AppCompatActivity(), OnItemSelectedListener, OnClickListener,
    LocationListenerCompat {

    private val GPS_PERMISSION_CODE: Int = 612

    private lateinit var popupManager: PopupManager
    private lateinit var database: Database

    private lateinit var MainActionButton: Button
    private lateinit var FigureSelect: Spinner
    private lateinit var PointsSavingLog: LinearLayout
    private lateinit var AppLogScroller: ScrollView
    private lateinit var CurrentCalculationState: TextView
    private lateinit var CalculationStateContainer: LinearLayout
    private lateinit var WelcomeMessage: TextView

    private lateinit var layoutInflater: LayoutInflater

    private var locationManager: LocationManager? = null

    private var areaType: AreaType = AreaType.RECTANGLE
    private var actionType: ActionType = ActionType.START_RECORDING
    private var lastSavedLocation: Location? = null
    private var currentPoints: Int = 0

    private val locationProvider = LocationManager.NETWORK_PROVIDER


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        this.setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        this.layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        this.initializeComponents()

        this.popupManager = PopupManager(this)
        this.database = Database(this)
        this.locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

        this.restoreActivityState(savedInstanceState)
        this.configureComponents()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(ActivityStateKey.SAVED_POINTS.id, this.currentPoints)
        outState.putString(ActivityStateKey.ACTION_TYPE.id, this.actionType.name)
        outState.putString(ActivityStateKey.AREA_TYPE.id, this.areaType.name)

        if (this.lastSavedLocation != null) {
            outState.putDouble(ActivityStateKey.LAST_LATITUDE.id, this.lastSavedLocation!!.latitude)
            outState.putDouble(
                ActivityStateKey.LAST_LONGITUDE.id,
                this.lastSavedLocation!!.longitude
            )
        }
    }

    private fun restoreActivityState(bundle: Bundle?) {
        if (bundle != null) {
            try {
                this.currentPoints = bundle.getInt(ActivityStateKey.SAVED_POINTS.id, 0)
                this.actionType = ActionType.valueOf(
                    bundle.getString(
                        ActivityStateKey.ACTION_TYPE.id,
                        ActionType.START_RECORDING.name
                    )
                )
                this.areaType = AreaType.valueOf(
                    bundle.getString(
                        ActivityStateKey.AREA_TYPE.id,
                        AreaType.RECTANGLE.name
                    )
                )

                if (bundle.containsKey(ActivityStateKey.LAST_LATITUDE.id) && bundle.containsKey(
                        ActivityStateKey.LAST_LONGITUDE.id
                    )
                ) {
                    this.lastSavedLocation = Location(this.locationProvider).apply {
                        latitude = bundle.getDouble(ActivityStateKey.LAST_LATITUDE.id)
                        longitude = bundle.getDouble(ActivityStateKey.LAST_LONGITUDE.id)
                    }
                }
            } catch (_: Exception) {
            }

            when (this.actionType) {
                ActionType.WAITING_POINT, ActionType.SAVE_POINT -> {
                    this.startSavingPoints()
                }

                else -> {}
            }
        } else {
            this.database.clearPoints()
        }
    }

    private fun initializeComponents() {
        this.MainActionButton = findViewById(R.id.MainActionButton)
        this.FigureSelect = findViewById(R.id.FigureSelect)
        this.PointsSavingLog = findViewById(R.id.PointsSavingLog)
        this.CurrentCalculationState = findViewById(R.id.CurrentCalculationState)
        this.WelcomeMessage = findViewById(R.id.WelcomeMessage)
        this.CalculationStateContainer = findViewById(R.id.CalculationStateContainer)
        this.AppLogScroller = findViewById(R.id.AppLogScroller)
    }

    private fun configureComponents() {
        this.FigureSelect.setSelection(areaType.id)
        this.FigureSelect.onItemSelectedListener = this
        this.MainActionButton.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.MainActionButton -> {
                when (this.actionType) {
                    ActionType.START_RECORDING -> {
                        this.checkGPSPermission({
                            this.addLogMessage(
                                "${this.getText(R.string.hint_calculation_starting_date)}: ${
                                    Database.dateToTimestamp(
                                        Date()
                                    )
                                }"
                            )
                            this.startSavingPoints()
                        })
                    }

                    ActionType.SAVE_POINT -> {
                        this.checkGPSPermission({
                            if (this.locationManager?.isProviderEnabled(this.locationProvider) == true) {
                                this.addLogMessage(this.getText(R.string.state_point_loading))
                                try {
                                    this.locationManager?.removeUpdates(this)
                                } catch (_: Exception) {
                                }
                                this.locationManager?.requestSingleUpdate(
                                    this.locationProvider,
                                    this,
                                    this.mainLooper
                                )
                                this.actionType = ActionType.WAITING_POINT
                            } else {
                                this.showEnableGPSDialog()
                            }
                        }, {
                            this.stopSavingPoints()
                        })
                    }

                    ActionType.WAITING_POINT -> {}
                }
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            R.id.FigureSelect -> {
                val newAreaType = AreaType.getById(position)
                if (this.currentPoints > 0 && this.areaType != newAreaType) {
                    parent.setSelection(this.areaType.id)
                    this.showAreaTypeChangeConfirmDialog(newAreaType)
                } else {
                    this.areaType = newAreaType
                    this.updateCalculationState()
                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        // do nothing
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == GPS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.startSavingPoints()
            } else {
                this.showOpenSettingsDialog()
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        if (this.lastSavedLocation?.latitude != latitude && this.lastSavedLocation?.longitude != longitude) {
            this.updatePoints(location)
        } else {
            this.addLogMessage(this.getText(R.string.hint_position_hasnt_changed))
        }
        this.actionType = ActionType.SAVE_POINT
    }

    override fun onProviderEnabled(provider: String) {
        this.addLogMessage(this.getText(R.string.state_gps_provider_disabled))
    }

    override fun onProviderDisabled(provider: String) {
        this.addLogMessage(this.getText(R.string.state_gps_provider_enabled))
        this.actionType = ActionType.SAVE_POINT
    }

    private fun checkGPSPermission(onSuccess: () -> Unit = { }, onDenied: () -> Unit = { }) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                ),
                GPS_PERMISSION_CODE
            )
            onDenied()
        } else {
            onSuccess()
        }
    }

    private fun startSavingPoints() {
        this.MainActionButton.setText(R.string.action_save_point)
        this.actionType = ActionType.SAVE_POINT
        this.PointsSavingLog.visibility = VISIBLE
        this.CalculationStateContainer.visibility = VISIBLE
        this.WelcomeMessage.visibility = GONE
        this.updateCalculationState()
    }

    private fun stopSavingPoints(clearPoints: Boolean = false) {
        this.MainActionButton.setText(R.string.action_start_recording)
        this.actionType = ActionType.START_RECORDING
        this.addLogMessage(
            "${this.getText(R.string.hint_calculation_stop_date)}: ${
                Database.dateToTimestamp(
                    Date()
                )
            }"
        )
        this.CalculationStateContainer.visibility = GONE

        if (clearPoints) {
            this.clearSavedPoints()
        }
    }

    private fun addLogMessage(message: CharSequence) {
        val logTextView = TextView(this)
        logTextView.text = message
        logTextView.setPadding(
            0,
            0,
            0,
            this.resources.getDimensionPixelSize(R.dimen.padding_small)
        )
        this.PointsSavingLog.addView(logTextView)
        this.AppLogScroller.post {
            this.AppLogScroller.smoothScrollTo(0, this.PointsSavingLog.bottom)
        }
    }

    private fun showOpenSettingsDialog() {
        this.popupManager.dialog(
            R.string.hint_no_gps_permission,
            R.string.error_gps_unavailable
        ) {
            setPositiveButton(R.string.settings) { _, _ ->
                val intent = Intent(Settings.ACTION_SETTINGS)
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                }
            }

            setNegativeButton(R.string.action_cancel) { dialog, _ ->
                dialog.dismiss()
            }
        }
    }

    private fun showEnableGPSDialog() {
        this.popupManager.dialog(R.string.hint_enable_gps, R.string.error_gps_unavailable) {
            setPositiveButton(R.string.settings) { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                }
            }

            setNegativeButton(R.string.action_cancel) { dialog, _ ->
                dialog.dismiss()
            }
        }
    }

    private fun showAreaTypeChangeConfirmDialog(newAreaType: AreaType) {
        this.popupManager.dialog(R.string.confirm_change_area_type, R.string.area_type_change) {
            setPositiveButton(R.string.dialog_ok_button_text) { _, _ ->
                this@MainActivity.FigureSelect.setSelection(newAreaType.id)
                this@MainActivity.areaType = newAreaType
                this@MainActivity.popupManager.toast(R.string.success_change_area_type)
                this@MainActivity.clearSavedPoints()
            }

            setNegativeButton(R.string.action_cancel) { dialog, _ ->
                dialog.dismiss()
            }
        }
    }

    private fun updateCalculationState() {
        this.CurrentCalculationState.text = getString(
            R.string.state_gained_points,
            this.currentPoints,
            this.areaType.pointsRequired
        )
    }

    private fun clearSavedPoints() {
        this.database.clearPoints()
        this.currentPoints = 0
        this.updateCalculationState()
    }

    private fun updatePoints(location: Location) {
        this.database.savePoint(
            location.latitude.toFloat(),
            location.longitude.toFloat(),
            Database.dateToTimestamp(Date())
        )
        this.currentPoints = this.database.getSavedPointsAmount()
        this.lastSavedLocation = location
        this.updateCalculationState()

        this.addLogMessage(
            "${this.getText(R.string.latitude)}: ${location.latitude}, ${
                this.getText(
                    R.string.longitude
                )
            }: ${location.longitude}"
        )
        if (this.currentPoints >= this.areaType.pointsRequired) {
            this.calculateArea()
        }
    }

    private fun calculateArea() {
        try {
            val figure = FigureManager.getFigure(
                this.areaType,
                FigureManager.convertCoordinates(
                    this.database.getAllPoints(),
                    this.locationProvider
                )
            )
            val area = figure.calcArea()
            this.popupManager.dialog(this.getString(R.string.area_result, area), R.string.area) {
                val inflated =
                    this@MainActivity.layoutInflater.inflate(R.layout.figure_dialog, null)
                val figureCanvas: FigureView = inflated.findViewById(R.id.FigureCanvas)
                setView(inflated)
                figureCanvas.setFigure(figure)
            }
            this.database.saveResult(
                UUID.randomUUID().toString(),
                area,
                Database.dateToTimestamp(Date())
            )
            this.lastSavedLocation = null
        } catch (err: Exception) {
            when (err) {
                is IndexOutOfBoundsException -> {
                    popupManager.dialog(
                        R.string.hint_not_enough_points,
                        R.string.error_area_calculation
                    )
                    return
                }

                is NumberFormatException -> popupManager.dialog(
                    R.string.hint_area_not_a_number,
                    R.string.error_area_calculation
                )

                else -> popupManager.toast(R.string.error_area_calculation)
            }
        }
        this.clearSavedPoints()
    }
}