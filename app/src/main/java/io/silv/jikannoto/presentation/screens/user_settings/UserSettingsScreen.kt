import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.silv.jikannoto.presentation.atoms.InteractionConsumingBox
import io.silv.jikannoto.presentation.components.*
import io.silv.jikannoto.presentation.components.AnimatedHintTextField
import io.silv.jikannoto.presentation.components.SettingToggle
import io.silv.jikannoto.presentation.navigation.Screens
import io.silv.jikannoto.presentation.screens.user_settings.UserSettingsScreenEffect
import io.silv.jikannoto.presentation.screens.user_settings.UserSettingsViewModel
import io.silv.jikannoto.ui.theme.LocalCustomTheme
import io.silv.jikannoto.ui.theme.LocalSpacing
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun UserSettingsScreen(
    authed: Boolean,
    viewModel: UserSettingsViewModel = getViewModel(),
    onNavigate: (Screens) -> Unit
) {

    val state = viewModel.collectAsState().value

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is UserSettingsScreenEffect.Navigate -> onNavigate(sideEffect.route)
        }
    }
    val scaffoldState = rememberBottomSheetScaffoldState()
    LaunchedEffect(key1 = true) {
        if (authed) scaffoldState.bottomSheetState.collapse()
    }

    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxSize()) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 90.dp,
            backgroundColor = Color.White,
            sheetShape = RoundedCornerShape(30.dp),
            content = {
                InteractionConsumingBox(
                    backgroundColor = LocalCustomTheme.current.userSettingsBackGround
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 60.dp, start = 20.dp, end = 20.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Text(
                            text = "User Settings",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalCustomTheme.current.text
                        )
                        Spacer(modifier = Modifier.height(22.dp))
                        SettingToggle(
                            setting = "use dark theme",
                            toggled = state.settings.darkTheme,
                            onToggle = {
                                viewModel.changeDarkTheme(state.settings.darkTheme)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        )
                        SettingToggle(
                            setting = "sync notos offline",
                            toggled = state.settings.alwaysSync,
                            onToggle = {
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        )
                        AnimatedHintTextField(
                            text = state.settings.firstName,
                            hint = "first name",
                            textChangeHandler = {
                                viewModel.changeFirstName(it)
                            }
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        AnimatedHintTextField(
                            text = state.settings.lastName,
                            hint = "last name",
                            textChangeHandler = {
                                viewModel.changeLastName(it)
                            }
                        )
                    }
                }
            },
            sheetBackgroundColor = LocalCustomTheme.current.drawer,
            sheetContent = {
                Column(
                    Modifier
                        .fillMaxHeight(0.8f)
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedLoginHeader(
                        scaffoldState,
                        expanded = scaffoldState.bottomSheetState.isExpanded,
                        onSettingsIconClick = {
                            scope.launch {
                                scaffoldState.bottomSheetState.collapse()
                            }
                        },
                        onUpArrowClick = {
                            scope.launch { scaffoldState.bottomSheetState.expand() }
                        }
                    )
                    Column(
                        Modifier
                            .clickable(MutableInteractionSource(), null) {
                                focusManager.clearFocus()
                            }
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Sign up to sync notes across devices, and,\nsave personalization settings as well.",
                            color = LocalCustomTheme.current.text,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 22.dp, bottom = LocalSpacing.current.xl)
                        )
                        AnimatedHintTextField(
                            Modifier
                                .fillMaxWidth()
                                .padding(end = 12.dp, bottom = LocalSpacing.current.m),
                            hint = "Enter your email",
                            text = state.username,
                            error = state.error,
                            textChangeHandler = { text ->
                                viewModel.usernameTextHandler(text)
                            }
                        )
                        AnimatedHintTextField(
                            Modifier
                                .fillMaxWidth()
                                .padding(end = 12.dp),
                            hint = "Enter your password",
                            text = state.password,
                            error = state.error,
                            textChangeHandler = { text ->
                                viewModel.passwordTextHandler(text)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    AnimatedVisibility(visible = state.errorMessage.isNotEmpty()) {
                        Text(
                            text = state.errorMessage,
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }
                    AnimatedButton(
                        modifier = Modifier.fillMaxWidth(0.7f).height(35.dp),
                        fontSize = 18f,
                        enabled = !state.authInProgress,
                        label = "continue",
                        onClick = {
                            viewModel.authenticate(state.username, state.password)
                        }
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
val BottomSheetScaffoldState.currentFraction: Float
    get() {
        val fraction = bottomSheetState.progress.fraction
        val targetValue = bottomSheetState.targetValue
        val currentValue = bottomSheetState.currentValue

        return when {
            currentValue == BottomSheetValue.Collapsed && targetValue == BottomSheetValue.Collapsed -> 0f
            currentValue == BottomSheetValue.Expanded && targetValue == BottomSheetValue.Expanded -> 1f
            currentValue == BottomSheetValue.Collapsed && targetValue == BottomSheetValue.Expanded -> fraction
            else -> 1f - fraction
        }
    }
