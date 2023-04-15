@file:OptIn(ExperimentalMaterialApi::class)

package io.wongxd.solution.compose.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.wongxd.solution.compose.composeTheme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
fun getBottomSheetState() = ModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden,
    animationSpec = SwipeableDefaults.AnimationSpec,
    confirmStateChange = { true })

fun ModalBottomSheetState.hideBottomSheetTryOrElse(elseLogic: () -> Boolean): Boolean {
    return if (this.isVisible) {
        needCloseSheetTime.value = System.currentTimeMillis()
        true
    } else
        elseLogic.invoke()
}

private val needCloseSheetTime = mutableStateOf(0L)

@Composable
fun FbBottomSheet(
    title: String = "操作选择",
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    sheetState: ModalBottomSheetState,
    itemList: List<Pair<String, () -> Unit>>,
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

                itemList.forEach { pair ->

                    oItem(des = pair.first) {
                        hideSheet()
                        pair.second.invoke()
                    }

                }

                Spacer(modifier = Modifier.height(12.dp))

                oItem(des = "取消", black = true) {
                    hideSheet()
                }

                Spacer(modifier = Modifier.height(32.dp))

            }
        }) { content() }

    LaunchedEffect(key1 = needCloseSheetTime.value, block = {
        hideSheet()
    })
}

@Composable
private fun ColumnScope.oItem(
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