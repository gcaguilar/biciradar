package com.gcaguilar.biciradar

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.gcaguilar.biciradar.core.SurfaceSnapshotBundle

internal object AndroidDynamicShortcuts {
  fun publish(context: Context, snapshot: SurfaceSnapshotBundle?) {
    val maxShortcutCount = ShortcutManagerCompat
      .getMaxShortcutCountPerActivity(context)
      .takeIf { it > 0 }
      ?: DEFAULT_MAX_DYNAMIC_SHORTCUTS
    val shortcuts = dynamicShortcutSpecs(
      snapshot = snapshot,
      maxShortcutCount = maxShortcutCount,
    ).map { spec ->
      shortcut(
        context = context,
        id = spec.id,
        shortLabel = spec.shortLabel,
        longLabel = spec.longLabel,
        iconRes = spec.iconRes,
        uri = Uri.parse(spec.uri),
        rank = spec.rank,
      )
    }
    ShortcutManagerCompat.setDynamicShortcuts(context, shortcuts)
  }

  private fun shortcut(
    context: Context,
    id: String,
    shortLabel: String,
    longLabel: String,
    iconRes: Int,
    uri: Uri,
    rank: Int,
  ): ShortcutInfoCompat {
    val intent = Intent(Intent.ACTION_VIEW, uri, context, MainActivity::class.java)
    return ShortcutInfoCompat.Builder(context, id)
      .setShortLabel(shortLabel)
      .setLongLabel(longLabel)
      .setIcon(IconCompat.createWithResource(context, iconRes))
      .setRank(rank)
      .setIntent(intent)
      .build()
  }
}
