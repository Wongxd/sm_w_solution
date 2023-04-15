package io.wongxd.solution.compose.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.wongxd.solution.compose.common.debouncedClickable
import io.wongxd.solution.compose.composeTheme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BatBottomSheet(
    coroutineScope: CoroutineScope,
    sheetState: ModalBottomSheetState,
    needCloseSheetTime: Long,
    title: String,
    items: @Composable ColumnScope.() -> Unit,
    content: @Composable () -> Unit
) {

    fun hideSheet() {
        coroutineScope.launch {
            sheetState.hide()
        }
    }

    ModalBottomSheetLayout(sheetState = sheetState,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.textStyle.sp12.appGrayLight(),
                    modifier = Modifier.padding(top = 11.dp, bottom = 10.dp)
                )

                items()

                Spacer(modifier = Modifier.height(60.dp))

            }
        }) { content() }

    LaunchedEffect(key1 = needCloseSheetTime, block = {
        hideSheet()
    })
}

@Composable
fun ColumnScope.BatBottomSheetItem(
    des: String, red: Boolean = false, black: Boolean = false, click: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 22.dp)
            .padding(top = 10.dp)
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorAssets.appPartColor)
            .debouncedClickable { click.invoke() }, Alignment.Center
    ) {
        Text(
            text = des,
            style = if (black) MaterialTheme.textStyle.sp16.black() else if (red) MaterialTheme.textStyle.sp16.appRed() else MaterialTheme.textStyle.sp16.appColor()
        )
    }
}