
package io.wongxd.solution.compose.common

import androidx.compose.foundation.background

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@Composable
fun LargerDialog(
    dialogOpen: MutableState<Boolean>
) {
    Dialog(onDismissRequest = { dialogOpen.value = false }) {
        Card( // or Surface
            elevation = 8.dp,
            modifier = Modifier
                .requiredWidth(LocalConfiguration.current.screenWidthDp.dp * 0.96f)
                .padding(4.dp)
                .background(Color.Red)
        ) {
            // content
        }
    }
}

@Preview(showBackground = true)
@ExperimentalMaterialApi
@Composable
fun ModalBottomSheetLayoutDemo() {
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()
    ModalBottomSheetLayout(sheetState = sheetState,
        sheetShape = RoundedCornerShape(10.dp),
        sheetContent = {
            Column(
                modifier = Modifier
                    .height(600.dp)
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                ) {
                    Text(
                        text = "评论",
                        fontSize = 18.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "close",
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .debouncedClickable {
                                coroutineScope.launch {
                                    sheetState.hide()
                                }
                            }
                    )
                }
                LazyColumn {
                    items(100) { index ->
                        ListItem {
                            Text("sheet content $index")
                        }
                    }
                }
            }
        }) {
        Column {
            Text("title")
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = {
                coroutineScope.launch {
                    sheetState.show()
                }
            }) {
                Text(text = "show")
            }
        }
    }
}