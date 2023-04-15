package io.wongxd.solution.compose.helper

import android.annotation.SuppressLint
import android.content.Context
import android.location.*
import android.os.Bundle
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import io.wongxd.solution.logger.Logger
import io.wongxd.solution.permission.PermissionCommons
import java.util.*


class GpsHelper(
    private val justWifi: Boolean = false,
    private val ctx: Context,
    private val getAltitude: (Double) -> Unit,
    private val onGetInfo: (location: Location, lat: Double, lon: Double, address: Address?) -> Unit
) {
    private val TAG = "GpsHelper"

    //1.获取位置管理器
    private val locationManager: LocationManager by lazy { ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    private var locationProvider: String = LocationManager.GPS_PROVIDER

    //获取地址信息:城市、街道等信息
    private fun getAddress(location: Location?): List<Address?>? {
        var result: List<Address?>? = null
        try {
            if (location != null) {
                val gc = Geocoder(ctx, Locale.getDefault())
                result = gc.getFromLocation(location.latitude, location.longitude, 1)
                Logger.d("$TAG,获取地址信息：$result，高度:${location.altitude}")
                callbackAltitude(location)
                onGetInfo.invoke(
                    location,
                    location.latitude,
                    location.longitude,
                    result?.filterNotNull()?.first()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    private fun callbackAltitude(location: Location?) {
        location?.altitude?.let {
            if (it > 0 || it < 0) {
                getAltitude.invoke(it)
            }
        }
    }

    private fun initLocationProvider() {
        val criteria = Criteria() // 条件对象，即指定条件过滤获得LocationProvider
        criteria.accuracy = Criteria.ACCURACY_FINE // 较高精度
        criteria.isAltitudeRequired = true // 是否需要高度信息
        criteria.isBearingRequired = true // 是否需要方向信息
        criteria.isCostAllowed = true // 是否产生费用
        criteria.powerRequirement = Criteria.POWER_HIGH // 设置耗电
        // 获取条件最好的Provider,若没有权限，mLocationProvider 为 null
        locationProvider =
            locationManager.getBestProvider(criteria, true) ?: LocationManager.GPS_PROVIDER


        if (justWifi) {
            locationProvider = LocationManager.NETWORK_PROVIDER
        }

        Logger.e("$TAG,定位方式 mLocationProvider = $locationProvider")
    }

    @SuppressLint("MissingPermission")
    fun getLocation() {
        //2.获取位置提供器，GPS或是NetWork
//        val providers = locationManager.getProviders(true)
//        if (providers.contains(LocationManager.GPS_PROVIDER)) {
//            //如果是GPS
//            locationProvider = LocationManager.GPS_PROVIDER
//            Logger.d(TAG, "定位方式GPS")
//        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
//            //如果是Network
//            locationProvider = LocationManager.NETWORK_PROVIDER
//            Logger.d(TAG, "定位方式Network")
//        } else {
//            Toast.makeText(ctx, "没有可用的位置提供器", Toast.LENGTH_SHORT).show()
//            return
//        }

        locationProvider = LocationManager.GPS_PROVIDER

        if (justWifi) {
            locationProvider = LocationManager.NETWORK_PROVIDER
        }


//        initLocationProvider()

        val location: Location? = locationManager.getLastKnownLocation(locationProvider)
        if (location != null) {
            Logger.d("$TAG,获取上次的位置-经纬度：${location.longitude},${location.latitude},高度:${location.altitude}")
            getAddress(location)
        }
        //监视地理位置变化，第二个和第三个参数分别为更新的最短时间minTime和最短距离minDistace
        locationManager.requestLocationUpdates(locationProvider, 3000, 10f, locationListener)

    }

    private val locationListener: LocationListener by lazy {
        object : LocationListener {

            @SuppressLint("MissingPermission")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                super.onStatusChanged(provider, status, extras)
                if (status != LocationProvider.OUT_OF_SERVICE) {
//                    updateLocation(mLocationManager.getLastKnownLocation(mLocationProvider))
                    locationManager.getLastKnownLocation(locationProvider)?.let {
                        getAddress(it)
                    }

                }
            }

            // Provider被enable时触发此函数，比如GPS被打开
            override fun onProviderEnabled(provider: String) {
                Logger.d("$TAG,onProviderEnabled,${provider}")
            }

            // Provider被disable时触发此函数，比如GPS被关闭
            override fun onProviderDisabled(provider: String) {
                Logger.d("$TAG,onProviderDisabled,${provider}")
            }

            //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
            override fun onLocationChanged(location: Location) {
                //如果位置发生变化，重新显示地理位置经纬度
                Logger.d("$TAG,监视地理位置变化-经纬度：${location.longitude},${location.latitude},${location.altitude}")
                getAddress(location)
            }
        }
    }

    fun onActive() {
        if (PermissionCommons.checkPermissionGranted(
                ctx,
                *PermissionCommons.REQUIRED_GPS_PERMISSIONS
            )
        ) getLocation()
    }

    fun onDeActive() {
        try {
            locationManager.removeUpdates(locationListener)
        } catch (e: Exception) {
        }
    }
}


@Composable
fun GpsCompose(
    justWifi: Boolean = false,
    onChange: (location: Location, lat: Double, lon: Double, address: Address?) -> Unit
) {
    val ctx = LocalContext.current

    var rememberAltitude by mutableDataSaverStateOf(
        DataSaverPreferences(ctx),
        "GpsCompose.rememberAltitude",
        "0.0"
    )

    val gpsHelper = remember {
        GpsHelper(justWifi, ctx, getAltitude = {
            rememberAltitude = it.toString()
        }, { location, lat, lon, address ->
            onChange.invoke(location.apply {
                this.altitude = rememberAltitude.toDouble()
            }, lat, lon, address)
        })
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, gpsHelper) {
        // Make it follow the current lifecycle
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    gpsHelper.onActive()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    gpsHelper.onDeActive()
                }
                else -> {}
            }
        }

        lifecycle.addObserver(lifecycleObserver)

        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }
}