package tv.projectivy.plugin.wallpaperprovider.bingwallpaper

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import tv.projectivy.plugin.wallpaperprovider.api.Event
import tv.projectivy.plugin.wallpaperprovider.api.IWallpaperProviderService
import tv.projectivy.plugin.wallpaperprovider.api.Wallpaper
import tv.projectivy.plugin.wallpaperprovider.api.WallpaperDisplayMode
import tv.projectivy.plugin.wallpaperprovider.api.WallpaperType
import java.lang.reflect.Type

class WallpaperProviderService: Service() {

    val gson by lazy { Gson() }
    val wallpaperListType: Type = object : TypeToken<List<BingWallpaper>>() {}.type

    override fun onCreate() {
        super.onCreate()
        PreferencesManager.init(this)
        NetClientManager.init(this)
    }

    override fun onBind(intent: Intent): IBinder {
        // Return the interface.
        return binder
    }

    private val binder = object : IWallpaperProviderService.Stub() {
        override fun getWallpapers(event: Event?): List<Wallpaper> {
            // Don't care about the event : as the updateMode was declared as update_mode_time_elapsed
            // We will only receive this event, so no need to filter out other events
            return runBlocking {
                fetchWallpapers()
            }
        }

        override fun getPreferences(): String {
            return PreferencesManager.export()
        }

        override fun setPreferences(params: String) {
            PreferencesManager.import(params)
        }

    }

    suspend fun fetchWallpapers(): List<Wallpaper> {
        val jsonString = loadJson(PreferencesManager.wallpaperSourceUrl)
        return parseJson(jsonString)
    }

    suspend fun loadJson(url: String): String? {
        return withContext(Dispatchers.IO) {
            NetClientManager.request(url)
        }
    }

    suspend fun parseJson(jsonString: String?): List<Wallpaper> {
        return withContext(Dispatchers.Default) {
            try {
                // Parse the JSON into a list of Wallpaper objects
                //gson.fromJson<List<BingWallpaper>?>(jsonString, wallpaperListType)
                // Convert the list of BingWallpaper objects into a list of Wallpaper objects

                // Parse the JSON into a map containing the list of BingWallpaper objects
                val jsonObject = gson.fromJson<Map<String, Any>>(jsonString, object : TypeToken<Map<String, Any>>() {}.type)
                val imagesJson = gson.toJson(jsonObject["images"])
                val bingWallpapers = gson.fromJson<List<BingWallpaper>>(imagesJson, object : TypeToken<List<BingWallpaper>>() {}.type)

                // Convert the list of BingWallpaper objects into a list of Wallpaper objects
                bingWallpapers
                ?.map { Wallpaper("https://www.bing.com"+it.url, WallpaperType.IMAGE,
                    WallpaperDisplayMode.DEFAULT, it.title, it.copyrightlink, it.copyright) }
                // Filter out any wallpapers with an empty URI
                ?.filter { it.uri.isNotBlank() }
                ?: emptyList()
            } catch (e: Exception) {
                Log.e("JSON Parsing", "Error parsing JSON: $e")
                emptyList()
            }
        }
    }
}