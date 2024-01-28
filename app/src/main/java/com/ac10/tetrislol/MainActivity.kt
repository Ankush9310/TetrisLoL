package com.ac10.tetrislol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ac10.tetrislol.logic.Direction
import com.ac10.tetrislol.logic.SoundUtil
import com.ac10.tetrislol.logic.StatusBarUtil
import com.ac10.tetrislol.logic.viewmodel.Action
import com.ac10.tetrislol.logic.viewmodel.GameMainViewModel
import com.ac10.tetrislol.ui.GameBody
import com.ac10.tetrislol.ui.GameScreen
import com.ac10.tetrislol.ui.PreviewGamescreen
import com.ac10.tetrislol.ui.combinedClickable
import com.ac10.tetrislol.ui.theme.TetrisLoLTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        StatusBarUtil.transparentStatusBar(this)
        SoundUtil.init(this)

        setContent {
            TetrisLoLTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel = viewModel<GameMainViewModel>()
                    val viewState = viewModel.viewState.value

                    LaunchedEffect(key1 = Unit) {
                        while (isActive) {
                            delay(650L - 55 * (viewState.level - 1))
                            viewModel.dispatch(Action.GameTick)
                        }
                    }

                    val localLifecycleOwner = LocalLifecycleOwner.current
                    DisposableEffect(key1 = Unit) {
                        val observer = object : DefaultLifecycleObserver {
                            override fun onPause(owner: LifecycleOwner) {
                                viewModel.dispatch(Action.Pause)
                            }

                            override fun onResume(owner: LifecycleOwner) {
                                viewModel.dispatch(Action.Resume)
                            }
                        }
                        localLifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                            localLifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }

                    GameBody(combinedClickable(
                        onMove = { direction: Direction ->
                            if (direction == Direction.Up) viewModel.dispatch(Action.Drop)
                            else viewModel.dispatch(Action.Move(direction))
                        },
                        onRotate = {
                            viewModel.dispatch(Action.Rotate)
                        },
                        onRestart = {
                            viewModel.dispatch(Action.Reset)
                        },
                        onPause = {
                            if (viewModel.viewState.value.isRunning) {
                                viewModel.dispatch(Action.Pause)
                            } else {
                                viewModel.dispatch(Action.Resume)
                            }
                        },
                        onMute = {
                            viewModel.dispatch(Action.Mute)
                        }
                    )) {
                            GameScreen(
                                modifier = Modifier.fillMaxSize()
                            )
                    }


                    
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SoundUtil.release()
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TetrisLoLTheme {
        GameBody {
            PreviewGamescreen(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}