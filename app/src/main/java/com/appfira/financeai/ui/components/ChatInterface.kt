package com.appfira.financeai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource

import com.appfira.financeai.model.TransactionType
import com.appfira.financeai.R
import com.appfira.financeai.ui.theme.Green
import com.appfira.financeai.ui.theme.Red
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.foundation.BorderStroke

@Composable
fun ChatInterface(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    paddingValues: PaddingValues,
    onSendMessage: (String, TransactionType?) -> Unit
) {
    var textState by remember { mutableStateOf("") }
    var forcedType by remember { mutableStateOf<TransactionType?>(null) }
    
    // Cache reversed list — hindari .reversed() setiap recompose (performa!)
    val reversedMessages = remember(messages) { messages.reversed() }
    
    val isKeyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val isSendEnabled = textState.isNotBlank() && forcedType != null

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            reverseLayout = true,
            contentPadding = PaddingValues(
                top = 16.dp, 
                bottom = 16.dp
            )
        ) {
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.CenterStart) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.chat_ai_typing), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            items(reversedMessages, key = { it.text + it.isUser }) { message ->
                ChatBubble(message)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = if (isKeyboardVisible) 6.dp else paddingValues.calculateBottomPadding() + 16.dp)
                .imePadding()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(top = 4.dp, bottom = 4.dp, start = 4.dp, end = 4.dp)) {
                    TextField(
                        value = textState,
                        onValueChange = { textState = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.chat_placeholder), fontSize = 13.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        maxLines = 4
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 8.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TypeChip(
                            label = stringResource(R.string.chat_hint_income),
                            icon = Icons.Default.Add,
                            isSelected = forcedType == TransactionType.INCOME,
                            color = Green,
                            onClick = { forcedType = if (forcedType == TransactionType.INCOME) null else TransactionType.INCOME }
                        )
                        TypeChip(
                            label = stringResource(R.string.chat_hint_expense),
                            icon = Icons.Default.Remove,
                            isSelected = forcedType == TransactionType.EXPENSE,
                            color = Red,
                            onClick = { forcedType = if (forcedType == TransactionType.EXPENSE) null else TransactionType.EXPENSE }
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        IconButton(
                            onClick = {
                                if (isSendEnabled) {
                                    onSendMessage(textState, forcedType)
                                    textState = ""
                                    forcedType = null
                                }
                            },
                            modifier = Modifier.size(40.dp),
                            enabled = isSendEnabled
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_send),
                                contentDescription = null,
                                tint = if (isSendEnabled) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypeChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent,
        border = BorderStroke(1.dp, if (isSelected) color else Color.Gray.copy(alpha = 0.1f)),
        modifier = Modifier.height(28.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = if (isSelected) color else Color.Gray, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) color else Color.Gray)
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.isUser
    val bgColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isUser) Color.Transparent else MaterialTheme.colorScheme.outline

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Icon(
                Icons.Default.SmartToy,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp).padding(end = 4.dp)
            )
        }

        Surface(
            color = bgColor,
            border = BorderStroke(1.2.dp, borderColor),
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isUser) 20.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 20.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                color = textColor,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }

        if (isUser) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp).padding(start = 4.dp)
            )
        }
    }
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)
