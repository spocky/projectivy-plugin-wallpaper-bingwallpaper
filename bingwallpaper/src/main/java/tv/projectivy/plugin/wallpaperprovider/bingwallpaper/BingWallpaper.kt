package tv.projectivy.plugin.wallpaperprovider.bingwallpaper

import kotlinx.serialization.Serializable

@Serializable
data class BingResponse(val images: List<BingWallpaper>)

@Serializable
data class BingWallpaper(val title: String,
                     val url: String,
                     val copyright: String,
                     val copyrightlink: String)
