package test.app.areacalculator

import DialogBuilder
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationListenerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import test.app.areacalculator.database.Database
import test.app.areacalculator.figures.Figure
import test.app.areacalculator.figures.FigureType
import test.app.areacalculator.figures.Oval
import test.app.areacalculator.figures.Rectangle
import test.app.areacalculator.figures.Triangle
import test.app.areacalculator.views.FigureView
import java.util.Date

class MainActivity : AppCompatActivity(), OnItemSelectedListener, OnClickListener,
    LocationListenerCompat {

    private lateinit var pointsDisplay: TextView
    private lateinit var logsDisplay: LinearLayout
    private lateinit var calculateButton: Button
    private lateinit var figureType: Spinner
    private lateinit var layoutInflater: LayoutInflater

    private var locationManager: LocationManager? = null

    private var areaType: FigureType = FigureType.RECTANGLE
    private var lastSavedLocation: Location? = null
    private var isWaitingPoint = false
    private val GPS_REQUEST_CODE: Int = 1

    private lateinit var dialogBuilder: DialogBuilder
    private lateinit var database: Database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        this.setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        this.calculateButton = findViewById(R.id.calculate_button)
        this.figureType = findViewById(R.id.figure_type)
        this.pointsDisplay = findViewById(R.id.saved_points)
        this.logsDisplay = findViewById(R.id.logs_display)

        this.layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        this.dialogBuilder = DialogBuilder(this)
        this.database = Database(this)
        this.locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

        this.restoreActivityState(savedInstanceState)

        this.figureType.setSelection(areaType.id)
        this.figureType.onItemSelectedListener = this
        this.calculateButton.setOnClickListener(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("area_type", this.areaType.name)

        if (this.lastSavedLocation != null) {
            outState.putDouble("last_latitude", this.lastSavedLocation!!.latitude)
            outState.putDouble(
                "last_longitude",
                this.lastSavedLocation!!.longitude
            )
        }
    }

    fun restoreActivityState(bundle: Bundle?) {
        if (bundle != null) {
            try {
                this.areaType = FigureType.valueOf(
                    bundle.getString(
                        "area_type",
                        FigureType.RECTANGLE.name
                    )
                )
                if (bundle.containsKey("last_latitude") && bundle.containsKey(
                        "last_longitude"
                    )
                ) {
                    val pos = Location(LocationManager.NETWORK_PROVIDER)
                    pos.latitude = bundle.getDouble("last_latitude")
                    pos.longitude = bundle.getDouble("last_longitude")
                    this.lastSavedLocation = pos
                }
            } catch (_: Exception) { }
        } else {
            this.database.clearPoints()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onClick(view: View?) {
        if (view?.id == R.id.calculate_button)  {
            val isAllowed = this.hasPermissionToGPS();
            if (isAllowed) {
                if (this.locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true) {
                    Toast.makeText(this, R.string.point_loading, Toast.LENGTH_SHORT).show()
                    try {
                        this.locationManager?.removeUpdates(this)
                    } catch (_: Exception) { }
                    this.locationManager?.requestSingleUpdate(
                        LocationManager.NETWORK_PROVIDER,
                        this,
                        this.mainLooper
                    )
                    this.isWaitingPoint = true
                } else {
                    this.createEnableGPSDialog()
                }
            } else {
                this.stopSavingPoints()
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (parent?.id == R.id.figure_type) {
            this.areaType = FigureType.getById(position)
            this.stopSavingPoints()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) { }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == GPS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                this.createSettingsDialog()
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        if (this.lastSavedLocation?.latitude != latitude && this.lastSavedLocation?.longitude != longitude) {
            this.savePointToDatabase(location)
        } else {
            Toast.makeText(this, R.string.position_not_changed, Toast.LENGTH_SHORT).show()
        }
        this.isWaitingPoint = false
    }

    override fun onProviderEnabled(provider: String) { }

    override fun onProviderDisabled(provider: String) {
        Toast.makeText(this, R.string.gps_disabled, Toast.LENGTH_SHORT).show()
    }

    fun hasPermissionToGPS(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                ),
                GPS_REQUEST_CODE
            )
            return false
        }
        return true
    }

    fun stopSavingPoints() {
        this.database.clearPoints()
        this.logsDisplay.removeAllViews()
        this.lastSavedLocation = null
        this.updatePoinstView()
    }

    fun createSettingsDialog() {
        this.dialogBuilder.dialog(
            R.string.no_gps_permission,
            R.string.gps_unavailable
        ) {
            setPositiveButton(R.string.settings) { _, _ ->
                val intent = Intent(Settings.ACTION_SETTINGS)
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                }
            }

            setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
        }
    }

    fun createEnableGPSDialog() {
        this.dialogBuilder.dialog(R.string.enable_gps, R.string.gps_unavailable) {
            setPositiveButton(R.string.settings) { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                }
            }

            setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
        }
    }

   fun updatePoinstView() {
        this.pointsDisplay.text = getString(
            R.string.saved_points,
            this.database.getSavedPointsAmount(),
            this.areaType.pointsRequired
        )
    }

    fun savePointToDatabase(location: Location) {
        this.database.savePoint(
            location.latitude.toFloat(),
            location.longitude.toFloat(),
            Database.dateToTimestamp(Date())
        )
        this.lastSavedLocation = location

        this.addMessageToLog(
            "${this.getText(R.string.latitude)}: ${location.latitude}, ${
                this.getText(
                    R.string.longitude
                )
            }: ${location.longitude}"
        )

        val points = this.database.getSavedPointsAmount()
        if (points >= this.areaType.pointsRequired) {
            try {
                this.calculateArea()
            } catch (_: Exception) {
                Toast.makeText(this, R.string.area_calc_error, Toast.LENGTH_SHORT).show()
            }
            this.stopSavingPoints()
        }
        this.updatePoinstView()
    }

    fun calculateArea() {
        val points = this.database.getSavedPoints().map {
            val location = Location(LocationManager.NETWORK_PROVIDER)
            location.latitude = it.latitude
            location.longitude = it.longitude
            location
        }

        val figure: Figure = when (this.areaType) {
            FigureType.TRIANGLE -> {
                val side1 = points[0].distanceTo(points[1])
                val side2 = points[1].distanceTo(points[2])
                val side3 = points[2].distanceTo(points[0])
                Triangle(side1, side2, side3)
            }

            FigureType.RECTANGLE -> {
                val width = points[0].distanceTo(points[1])
                val height = points[1].distanceTo(points[2])
                Rectangle(width, height)
            }

            FigureType.SQUARE -> {
                val size = points[0].distanceTo(points[1])
                Rectangle(size)
            }

            FigureType.CIRCLE -> {
                val radius = points[0].distanceTo(points[1])
                Oval(radius)
            }

            FigureType.OVAL -> {
                val radius1 = points[0].distanceTo(points[1])
                val radius2 = points[1].distanceTo(points[2])
                Oval(radius1, radius2)
            }
        }

        val area = figure.calcArea()
        this.dialogBuilder.dialog(this.getString(R.string.area_result, area), R.string.result) {
            val dialogContent = this@MainActivity.layoutInflater.inflate(R.layout.result_dialog, null)
            val figureView: FigureView = dialogContent.findViewById(R.id.FigureCanvas)
            figureView.setFigure(figure)
            setView(dialogContent)
        }
    }

    fun addMessageToLog(message: CharSequence) {
        val logTextView = TextView(this)
        logTextView.text = message
        logTextView.setPadding(
            0,
            0,
            0,
            20
        )
        this.logsDisplay.addView(logTextView)
    }
}