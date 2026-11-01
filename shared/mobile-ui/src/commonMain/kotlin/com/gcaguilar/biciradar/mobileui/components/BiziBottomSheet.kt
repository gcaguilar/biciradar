package com.gcaguilar.biciradar.mobileui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.layout.LocalBiziWindowInfo

/**
 * Max width of a modal sheet on wide windows. Slightly narrower than the Material
 * default (640dp) so the sheet keeps a clear margin next to the navigation rail and
 * reads as a floating card instead of a full-width panel.
 */
private val BiziWideSheetMaxWidth = 560.dp

/**
 * Single entry point for modal bottom sheets in the app.
 *
 * Centralises the width used on wide windows and keeps every sheet consistent with the
 * adaptive layout. Sheets stay anchored to the window bottom, which is also the right
 * behaviour on foldables in tabletop posture: the content lands in the lower half instead
 * of straddling the hinge.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BiziBottomSheet(
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
  containerColor: Color = LocalBiziColors.current.surface,
  content: @Composable ColumnScope.() -> Unit,
) {
  val windowInfo = LocalBiziWindowInfo.current
  ModalBottomSheet(
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    sheetState = sheetState,
    containerColor = containerColor,
    sheetMaxWidth =
      if (windowInfo.isWide) {
        BiziWideSheetMaxWidth
      } else {
        BottomSheetDefaults.SheetMaxWidth
      },
    content = content,
  )
}
