package tv.projectivy.plugin.wallpaperprovider.bingwallpaper

import android.os.Bundle
import androidx.appcompat.content.res.AppCompatResources
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist.Guidance
import androidx.leanback.widget.GuidedAction

class SettingsFragment : GuidedStepSupportFragment() {
    override fun onCreateGuidance(savedInstanceState: Bundle?): Guidance {
        return Guidance(
            getString(R.string.plugin_short_name),
            getString(R.string.plugin_description),
            getString(R.string.settings),
            AppCompatResources.getDrawable(requireActivity(), R.mipmap.ic_banner)
        )
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        PreferencesManager.init(requireContext())

        val currentVideoSourceUrl = PreferencesManager.wallpaperSourceUrl
        GuidedAction.Builder(context)
            .id(ACTION_ID_WALLPAPER_SOURCE_URL)
            .title(R.string.setting_video_source)
            .description(currentVideoSourceUrl)
            .editDescription(currentVideoSourceUrl)
            .descriptionEditable(true)
            .build()
            .also { actions.add(it) }
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        when (action.id) {
            ACTION_ID_WALLPAPER_SOURCE_URL -> {
                val params: CharSequence? = action.editDescription
                findActionById(ACTION_ID_WALLPAPER_SOURCE_URL)?.description = params
                notifyActionChanged(findActionPositionById(ACTION_ID_WALLPAPER_SOURCE_URL))
                PreferencesManager.wallpaperSourceUrl = (params?: PreferencesManager.DEFAULT_WALLPAPER_SOURCE_URL).toString()
            }
        }
    }

    companion object {
        private const val ACTION_ID_WALLPAPER_SOURCE_URL= 1L
    }
}
