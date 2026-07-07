package com.appfira.financeai.ui.screens.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import android.app.Activity
import com.appfira.financeai.R
import com.appfira.financeai.model.AppIcon
import com.appfira.financeai.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.BatteryFull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconSubScreen(
    currentIcon: AppIcon,
    onBack: () -> Unit,
    onSetIcon: (AppIcon, Boolean) -> Unit
) {
    val view = LocalView.current
    val darkTheme = LocalDarkTheme.current
    var showRestartSheet by remember { mutableStateOf(false) }
    var pendingIcon by remember { mutableStateOf<AppIcon?>(null) }
    
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity
            activity?.let {
                val window = it.window
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
            }
        }

        DisposableEffect(darkTheme) {
            onDispose {
                val activity = view.context as? Activity
                activity?.let {
                    val window = it.window
                    val insetsController = WindowCompat.getInsetsController(window, view)
                    insetsController.isAppearanceLightStatusBars = !darkTheme
                }
            }
        }
    }

    if (showRestartSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRestartSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.RestartAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.icon_restart_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.icon_restart_desc),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { 
                            pendingIcon?.let { onSetIcon(it, false) }
                            showRestartSheet = false 
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.icon_later))
                    }
                    Button(
                        onClick = { 
                            pendingIcon?.let { onSetIcon(it, true) }
                            showRestartSheet = false 
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.icon_restart_now))
                    }
                }
            }
        }
    }

    val gradientBackground = Brush.verticalGradient(
        colors = listOf(LightBlue, DeepBlue)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(460.dp)
                .align(Alignment.TopCenter)
        ) {
            val w = size.width
            val h = size.height
            val cornerRadius = 40.dp.toPx() 

            val path = Path().apply {
                moveTo(0f, 0f) 
                lineTo(0f, h) 
                quadraticBezierTo(
                    0f, h - cornerRadius,
                    cornerRadius, h - cornerRadius 
                )
                lineTo(w - cornerRadius, h - cornerRadius) 
                quadraticBezierTo(
                    w, h - cornerRadius, 
                    w, h 
                )
                lineTo(w, 0f) 
                close() 
            }
            
            drawPath(
                path = path,
                brush = gradientBackground
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.btn_back),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.icon_select_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(380.dp)
                        .clip(RoundedCornerShape(36.dp))
                        .background(Color(0xFF222222))
                        .padding(6.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(DeepBlue, Color(0xFF0369A1)) 
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "09:41", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SignalCellular4Bar, null, tint = Color.White, modifier = Modifier.size(8.dp))
                                Icon(Icons.Default.Wifi, null, tint = Color.White, modifier = Modifier.size(8.dp))
                                Icon(Icons.Default.BatteryFull, null, tint = Color.White, modifier = Modifier.size(10.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                MockIconItem(label = stringResource(R.string.icon_mock_messages), color = Color(0xFF4ADE80))
                                MockIconItem(label = stringResource(R.string.app_name), color = Color.White, isAppIcon = true, currentIcon = currentIcon)
                                MockIconItem(label = stringResource(R.string.icon_mock_camera), color = Color(0xFF94A3B8))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                MockIconItem(label = stringResource(R.string.icon_mock_photos), color = Color(0xFFF87171))
                                MockIconItem(label = stringResource(R.string.icon_mock_notes), color = Color(0xFFFACC15))
                                MockIconItem(label = stringResource(R.string.icon_mock_music), color = Color(0xFFFB7185))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                MockIconItem(label = stringResource(R.string.icon_mock_maps), color = Color(0xFF60A5FA))
                                MockIconItem(label = stringResource(R.string.icon_mock_settings), color = Color(0xFF94A3B8))
                                MockIconItem(label = stringResource(R.string.icon_mock_clock), color = Color(0xFF334155))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                MockIconItem(label = stringResource(R.string.icon_mock_phone), color = Color(0xFF4ADE80))
                                MockIconItem(label = stringResource(R.string.icon_mock_mail), color = Color(0xFF38BDF8))
                                MockIconItem(label = stringResource(R.string.icon_mock_browser), color = Color(0xFFF97316))
                            }
                        }
                    }
                    Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 5.dp).size(6.dp).clip(CircleShape).background(Color.Black))
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                IconRowItem(
                    title = stringResource(R.string.icon_title_classic),
                    description = stringResource(R.string.icon_classic_desc),
                    iconRes = R.drawable.logo,
                    isSelected = currentIcon == AppIcon.DEFAULT,
                    onClick = { 
                        pendingIcon = AppIcon.DEFAULT
                        showRestartSheet = true
                    }
                )

                Divider(modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                IconRowItem(
                    title = stringResource(R.string.icon_title_dark),
                    description = stringResource(R.string.icon_dark_desc),
                    iconRes = R.drawable.logo_white,
                    isSelected = currentIcon == AppIcon.DARK,
                    onClick = { 
                        pendingIcon = AppIcon.DARK
                        showRestartSheet = true
                    }
                )

                Divider(modifier = Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                IconRowItem(
                    title = stringResource(R.string.icon_title_themed),
                    description = stringResource(R.string.icon_themed_desc),
                    iconRes = R.drawable.logo,
                    isSelected = currentIcon == AppIcon.THEMED,
                    onClick = { 
                        pendingIcon = AppIcon.THEMED
                        showRestartSheet = true
                    }
                )
            }
        }
    }
}

@Composable
fun MockIconItem(
    label: String, 
    color: Color, 
    isAppIcon: Boolean = false, 
    currentIcon: AppIcon = AppIcon.DEFAULT
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, 
        modifier = Modifier.width(42.dp)
    ) {
        if (isAppIcon) {
            val iconBgColor = when (currentIcon) {
                AppIcon.DEFAULT -> LightBlue
                AppIcon.DARK -> Color(0xFF334155)
                AppIcon.THEMED -> Color(0xFFCBD5E1) 
            }
            Image(
                painter = painterResource(
                    id = when (currentIcon) { 
                        AppIcon.DEFAULT -> R.drawable.logo 
                        AppIcon.DARK -> R.drawable.logo_white 
                        AppIcon.THEMED -> R.drawable.logo 
                    }
                ), 
                contentDescription = null, 
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBgColor)
                    .padding(4.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 8.sp, color = Color.White.copy(alpha = 0.9f), maxLines = 1)
    }
}

@Composable
fun IconRowItem(
    title: String, 
    description: String, 
    iconRes: Int, 
    isSelected: Boolean, 
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 20.dp), 
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconRes), 
            contentDescription = null, 
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(LightBlue)
                .padding(6.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title, 
                fontSize = 15.sp, 
                fontWeight = FontWeight.Bold, 
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description, 
                fontSize = 12.sp, 
                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                lineHeight = 16.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        RadioButton(
            selected = isSelected, 
            onClick = onClick, 
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        )
    }
}
