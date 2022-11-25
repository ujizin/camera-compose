package com.ujizin.camera_compose

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.ujizin.camera_compose.ui.theme.CameracomposeTheme
import com.ujizin.camposer.CameraPreview
import com.ujizin.camposer.state.CamSelector
import com.ujizin.camposer.state.CaptureMode
import com.ujizin.camposer.state.CaptureMode.*
import com.ujizin.camposer.state.rememberCameraState
import java.io.File

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CameracomposeTheme {
                val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA)

                LaunchedEffect(Unit) {
                    cameraPermission.launchPermissionRequest()
                }

                if (cameraPermission.status.isGranted) {
                    CameraUI()
                }
            }
        }
    }
}

@Composable
fun CameraUI(modifier: Modifier = Modifier) {
    val cameraState = rememberCameraState()
    var camSelector by remember { mutableStateOf(CamSelector.Back) }
    var captureMode by remember { mutableStateOf(Image) }
    val context = LocalContext.current
    CameraPreview(
        modifier = modifier,
        captureMode = captureMode,
        cameraState = cameraState,
        camSelector = camSelector,
    ) {
        CameraContent(
            captureMode = captureMode,
            onAction = {
                when (captureMode) {
                    Image -> {
                        cameraState.takePicture(context.createNewFile("jpg")) { result ->
                            Toast.makeText(context, "Result: $result", Toast.LENGTH_SHORT).show()
                        }
                    }

                    Video -> {
                        cameraState.toggleRecording(context.createNewFile("mp4")) { result ->
                            Toast.makeText(context, "Result: $result", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            },
            onSwitchCamera = { camSelector = camSelector.inverse },
            onCaptureModeChanged = {
                captureMode = when (captureMode) {
                    Image -> Video
                    Video -> Image
                }
            }
        )
    }
}

@Composable
fun CameraContent(
    captureMode: CaptureMode,
    onAction: () -> Unit,
    onCaptureModeChanged: () -> Unit,
    onSwitchCamera: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        val actionText = if (captureMode == Image) "take picture" else "record video"
        Button(onClick = onAction) { Text(actionText) }
        Button(onClick = onCaptureModeChanged) { Text("mode: $captureMode") }
        Button(onClick = onSwitchCamera) { Text("Switch camera") }
    }
}

private fun Context.createNewFile(extension: String) = File(
    filesDir, "${System.currentTimeMillis()}.$extension"
).apply { createNewFile() }
