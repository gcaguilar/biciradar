package com.gcaguilar.bizizaragoza

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.gcaguilar.bizizaragoza.mobileui.AssistantLaunchRequest
import com.gcaguilar.bizizaragoza.mobileui.MobileLaunchRequest

/**
 * Tracks assistant shortcut launches to Firebase Analytics.
 *
 * Events are sent unconditionally when the app is launched via a shortcut or
 * assistant intent. The "shortcut_fallback" event fires specifically when an
 * assistant launch ends up on the Atajos (shortcuts) tab without a resolved
 * station — which helps diagnose cases where the incoming intent has no
 * recognisable action or query.
 *
 * Firebase Analytics is only on the classpath when google-services.json is
 * present (see androidApp/build.gradle.kts), so all calls are guarded with a
 * try/catch to keep the build working without credentials.
 */
internal object ShortcutAnalytics {

  fun trackLaunch(
    context: Context,
    intent: Intent,
    launchRequest: MobileLaunchRequest?,
    assistantLaunchRequest: AssistantLaunchRequest?,
  ) {
    // Only track launches that came from the assistant or a shortcut.
    val hasShortcutExtra = intent.hasExtra(ASSISTANT_ACTION_EXTRA) ||
      intent.hasExtra(FEATURE_EXTRA) ||
      intent.data?.host == "assistant"
    if (!hasShortcutExtra) return

    val isFallback = launchRequest == MobileLaunchRequest.OpenAssistant &&
      assistantLaunchRequest == null

    val params = Bundle().apply {
      putString("intent_action", intent.action.orEmpty())
      putString("intent_data", intent.data?.toString().orEmpty())
      putString(
        "extra_assistant_action",
        intent.getStringExtra(ASSISTANT_ACTION_EXTRA).orEmpty(),
      )
      putString("extra_feature", intent.getStringExtra(FEATURE_EXTRA).orEmpty())
      putString(
        "extra_station_query",
        intent.getStringExtra(STATION_QUERY_EXTRA).orEmpty(),
      )
      putString(
        "extra_station_id",
        intent.getStringExtra(STATION_ID_EXTRA).orEmpty(),
      )
      putString("resolved_launch_request", launchRequest?.javaClass?.simpleName.orEmpty())
      putString(
        "resolved_assistant_request",
        assistantLaunchRequest?.javaClass?.simpleName.orEmpty(),
      )
      putBoolean("is_fallback_to_atajos", isFallback)
    }

    val eventName = if (isFallback) "shortcut_fallback" else "shortcut_launch"
    logEvent(context, eventName, params)
  }

  private fun logEvent(context: Context, name: String, params: Bundle) {
    try {
      val analyticsClass = Class.forName("com.google.firebase.analytics.FirebaseAnalytics")
      val getInstance = analyticsClass.getMethod("getInstance", Context::class.java)
      val analytics = getInstance.invoke(null, context)
      val logEvent = analyticsClass.getMethod("logEvent", String::class.java, Bundle::class.java)
      logEvent.invoke(analytics, name, params)
    } catch (_: ClassNotFoundException) {
      // Firebase Analytics not available in this build (no google-services.json).
    } catch (_: Exception) {
      // Silently swallow any other reflection or Firebase initialisation error.
    }
  }
}
