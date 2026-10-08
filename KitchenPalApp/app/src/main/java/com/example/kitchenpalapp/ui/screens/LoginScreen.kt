package com.example.kitchenpalapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kitchenpalapp.R
import com.example.kitchenpalapp.ui.components.ServerConfigDialog
import com.example.kitchenpalapp.ui.theme.Background
import com.example.kitchenpalapp.ui.theme.Border
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.PrimaryLighter
import com.example.kitchenpalapp.ui.theme.StateError
import com.example.kitchenpalapp.ui.theme.Surface
import com.example.kitchenpalapp.ui.theme.TextPrimary
import com.example.kitchenpalapp.ui.theme.TextSecondary
import com.example.kitchenpalapp.ui.theme.TextTertiary
import com.example.kitchenpalapp.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    viewModel: MainViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoginTab by remember { mutableStateOf(true) }
    var isForgotPasswordMode by remember { mutableStateOf(false) }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var agreedToTerms by remember { mutableStateOf(false) }
    var termsError by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var resetCode by remember { mutableStateOf("") }
    var resetCodeError by remember { mutableStateOf<String?>(null) }
    var resetStep by remember { mutableStateOf(1) }
    var showServerDialog by remember { mutableStateOf(false) }
    val serverUrl by viewModel.serverUrl.collectAsState()

    // 进入登录/注册页即载入已保存的服务器地址（右上角入口据此显示状态）
    LaunchedEffect(Unit) { viewModel.loadServerUrl() }

    fun validatePhone(): Boolean {
        return if (phone.isEmpty()) {
            phoneError = "请输入手机号或邮箱"
            false
        } else if (phone.contains("@")) {
            val emailRegex = "^[A-Za-z](.*)([@])(.+)(\\.)(.+)"
            if (!emailRegex.toRegex().matches(phone)) {
                phoneError = "请输入正确的邮箱格式"
                false
            } else {
                phoneError = null
                true
            }
        } else {
            val phoneRegex = "^1[3-9]\\d{9}$"
            if (!phoneRegex.toRegex().matches(phone)) {
                phoneError = "请输入正确的手机号格式"
                false
            } else {
                phoneError = null
                true
            }
        }
    }

    fun validatePassword(): Boolean {
        return if (password.isEmpty()) {
            passwordError = "请输入密码"
            false
        } else if (password.length < 6) {
            passwordError = "密码长度不能少于6位"
            false
        } else {
            passwordError = null
            true
        }
    }

    fun validateConfirmPassword(): Boolean {
        return if (!isLoginTab) {
            if (confirmPassword.isEmpty()) {
                confirmPasswordError = "请确认密码"
                false
            } else if (confirmPassword != password) {
                confirmPasswordError = "两次密码输入不一致"
                false
            } else {
                confirmPasswordError = null
                true
            }
        } else {
            confirmPasswordError = null
            true
        }
    }

    fun validateTerms(): Boolean {
        return if (!agreedToTerms) {
            termsError = true
            false
        } else {
            termsError = false
            true
        }
    }

    fun handleSubmit() {
        val isPhoneValid = validatePhone()
        val isPasswordValid = validatePassword()
        val isConfirmPasswordValid = validateConfirmPassword()
        val isTermsValid = validateTerms()

        if (isPhoneValid && isPasswordValid && isConfirmPasswordValid && isTermsValid) {
            isLoading = true
            coroutineScope.launch {
                try {
                    if (isLoginTab) {
                        val result = viewModel.login(phone, password)
                        if (result.isSuccess && result.data != null) {
                            isLoading = false
                            onLoginSuccess()
                        } else {
                            isLoading = false
                            android.widget.Toast.makeText(
                                context,
                                result.message ?: "登录失败，请检查账号密码",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        val result = viewModel.register(phone, password, "用户$phone")
                        if (result.isSuccess) {
                            isLoading = false
                            isLoginTab = true
                            android.widget.Toast.makeText(
                                context,
                                "注册成功，请登录",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            isLoading = false
                            android.widget.Toast.makeText(
                                context,
                                result.message ?: "注册失败，手机号可能已被注册",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    isLoading = false
                    android.widget.Toast.makeText(
                        context,
                        "网络错误：${e.message}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopIllustration(
            isCustomServer = serverUrl.isNotBlank(),
            onNetworkClick = { showServerDialog = true }
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                if (isForgotPasswordMode) {
                    ForgotPasswordForm(
                        phone = phone,
                        phoneError = phoneError,
                        resetCode = resetCode,
                        resetCodeError = resetCodeError,
                        password = password,
                        passwordError = passwordError,
                        confirmPassword = confirmPassword,
                        confirmPasswordError = confirmPasswordError,
                        resetStep = resetStep,
                        onPhoneChange = { phone = it; phoneError = null },
                        onResetCodeChange = { resetCode = it; resetCodeError = null },
                        onPasswordChange = { password = it; passwordError = null },
                        onConfirmPasswordChange = { confirmPassword = it; confirmPasswordError = null },
                        onSendCode = {
                            if (validatePhone()) {
                                resetStep = 2
                            }
                        },
                        onResetPassword = {
                            if (resetCode.isEmpty()) {
                                resetCodeError = "请输入验证码"
                            } else if (!validatePassword()) {
                            } else if (!validateConfirmPassword()) {
                            } else {
                                isForgotPasswordMode = false
                                android.widget.Toast.makeText(
                                    context,
                                    "密码重置成功",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onBack = {
                            isForgotPasswordMode = false
                            resetStep = 1
                        }
                    )
                } else {
                    TabSwitcher(
                        isLoginTab = isLoginTab,
                        onTabChange = {
                            isLoginTab = it
                            phoneError = null
                            passwordError = null
                            confirmPasswordError = null
                            termsError = false
                        }
                    )

                Spacer(modifier = Modifier.height(24.dp))

                LoginInputField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        if (phoneError != null) validatePhone()
                    },
                    placeholder = "请输入手机号/邮箱",
                    iconRes = R.drawable.smartphone,
                    isError = phoneError != null,
                    errorText = phoneError,
                    keyboardType = if (phone.contains("@")) KeyboardType.Email else KeyboardType.Phone
                )

                Spacer(modifier = Modifier.height(12.dp))

                LoginInputField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (passwordError != null) validatePassword()
                    },
                    placeholder = if (isLoginTab) "请输入密码" else "设置密码（至少6位）",
                    iconRes = R.drawable.lock,
                    isError = passwordError != null,
                    errorText = passwordError,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                    keyboardType = KeyboardType.Password
                )

                if (!isLoginTab) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LoginInputField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            if (confirmPasswordError != null) validateConfirmPassword()
                        },
                        placeholder = "请再次输入密码",
                        iconRes = R.drawable.lock,
                        isError = confirmPasswordError != null,
                        errorText = confirmPasswordError,
                        isPassword = true,
                        passwordVisible = confirmPasswordVisible,
                        onPasswordVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible },
                        keyboardType = KeyboardType.Password
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (agreedToTerms) Primary
                                else if (termsError) StateError.copy(alpha = 0.2f)
                                else Color.Transparent
                            )
                            .border(
                                width = 2.dp,
                                color = if (termsError) StateError else if (agreedToTerms) Primary else Border,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable {
                                agreedToTerms = !agreedToTerms
                                if (termsError) termsError = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (agreedToTerms) {
                            Image(
                                painter = painterResource(id = R.drawable.check),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(Color.White),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "我已阅读并同意",
                            fontSize = 12.sp,
                            color = if (termsError) StateError else TextTertiary
                        )
                        Text(
                            text = "《用户协议》",
                            fontSize = 12.sp,
                            color = Primary,
                            modifier = Modifier.clickable { onTermsClick() }
                        )
                        Text(
                            text = "和",
                            fontSize = 12.sp,
                            color = if (termsError) StateError else TextTertiary
                        )
                        Text(
                            text = "《隐私政策》",
                            fontSize = 12.sp,
                            color = Primary,
                            modifier = Modifier.clickable { onPrivacyClick() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(
                            if (isLoading) Primary.copy(alpha = 0.7f)
                            else Primary
                        )
                        .clickable(enabled = !isLoading) {
                            if (!isLoading) handleSubmit()
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isLoading) "登录中..." else if (isLoginTab) "登录" else "注册",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (isLoginTab) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "忘记密码？",
                            fontSize = 13.sp,
                            color = Primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable {
                                isForgotPasswordMode = true
                                resetStep = 1
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                DividerWithText("其他登录方式")

                Spacer(modifier = Modifier.height(20.dp))

                SocialLoginRow()
            }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }

    if (showServerDialog) {
        ServerConfigDialog(
            viewModel = viewModel,
            onDismiss = { showServerDialog = false }
        )
    }
}

@Composable
private fun TopIllustration(
    isCustomServer: Boolean = false,
    onNetworkClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PrimaryLighter, Background)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(elevation = 8.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(PrimaryLighter),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.chef_hat),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Primary),
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "料理助手",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "让每一道菜都恰到好处",
                fontSize = 13.sp,
                color = TextTertiary
            )
        }

        // 右上角网络配置入口：真机联调时登录前就能改后端地址
        NetworkConfigEntry(
            isCustom = isCustomServer,
            onClick = onNetworkClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 44.dp, end = 16.dp)
        )
    }
}

/**
 * 网络配置入口胶囊按钮：显示当前是否已自定义服务器地址。
 */
@Composable
private fun NetworkConfigEntry(
    isCustom: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Surface)
            .border(
                width = 1.dp,
                color = if (isCustom) Primary else Border,
                shape = RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_wifi),
            contentDescription = "网络设置",
            colorFilter = ColorFilter.tint(if (isCustom) Primary else TextTertiary),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = if (isCustom) "自定义网络" else "网络设置",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isCustom) Primary else TextTertiary
        )
    }
}

@Composable
private fun TabSwitcher(
    isLoginTab: Boolean,
    onTabChange: (Boolean) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        TabItem(
            text = "登录",
            isActive = isLoginTab,
            onClick = { onTabChange(true) }
        )
        TabItem(
            text = "注册",
            isActive = !isLoginTab,
            onClick = { onTabChange(false) }
        )
    }
}

@Composable
private fun TabItem(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isActive) Primary else TextTertiary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(2.dp)
                .background(if (isActive) Primary else Color.Transparent)
        )
    }
}

@Composable
private fun LoginInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    iconRes: Int,
    trailing: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorText: String? = null,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityToggle: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var isFocused by remember { mutableStateOf(false) }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Background)
                .border(
                    width = 1.5.dp,
                    color = when {
                        isError -> StateError
                        isFocused -> Primary
                        else -> Border
                    },
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(if (isError) StateError else TextTertiary),
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .size(20.dp)
                )
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        color = TextPrimary
                    ),
                    cursorBrush = SolidColor(Primary),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 14.dp)
                        .onFocusChanged { isFocused = it.isFocused },
                    decorationBox = { innerTextField ->
                        Box {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    color = TextTertiary,
                                    fontSize = 15.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                if (isPassword && onPasswordVisibilityToggle != null) {
                    Text(
                        text = if (passwordVisible) "隐藏" else "显示",
                        color = Primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable(onClick = onPasswordVisibilityToggle)
                    )
                } else if (trailing != null) {
                    trailing()
                } else {
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }
        }
        if (isError && errorText != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = errorText,
                color = StateError,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun DividerWithText(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(Border)
        )
        Text(
            text = text,
            fontSize = 12.sp,
            color = TextTertiary,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(Border)
        )
    }
}

@Composable
private fun SocialLoginRow() {
    val context = androidx.compose.ui.platform.LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SocialIcon(
            backgroundColor = Color(0xFF07C160),
            iconRes = R.drawable.ic_wechat,
            contentDescription = "微信登录",
            iconSize = 23.dp,
            onClick = {
                android.widget.Toast.makeText(context, "微信登录", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
        Spacer(modifier = Modifier.width(24.dp))
        SocialIcon(
            backgroundColor = Color(0xFF1D1D1F),
            iconRes = R.drawable.ic_apple,
            contentDescription = "Apple登录",
            iconSize = 22.dp,
            onClick = {
                android.widget.Toast.makeText(context, "Apple登录", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
        Spacer(modifier = Modifier.width(24.dp))
        SocialIcon(
            backgroundColor = Primary,
            iconRes = R.drawable.ic_phone_login,
            contentDescription = "手机登录",
            iconSize = 24.dp,
            withShadow = true,
            onClick = {
                android.widget.Toast.makeText(context, "手机登录", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun SocialIcon(
    backgroundColor: Color,
    iconRes: Int,
    contentDescription: String,
    iconSize: Dp = 22.dp,
    withShadow: Boolean = false,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .then(
                if (withShadow) Modifier.shadow(elevation = 4.dp, shape = CircleShape)
                else Modifier
            )
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            colorFilter = ColorFilter.tint(Color.White),
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
private fun ForgotPasswordForm(
    phone: String,
    phoneError: String?,
    resetCode: String,
    resetCodeError: String?,
    password: String,
    passwordError: String?,
    confirmPassword: String,
    confirmPasswordError: String?,
    resetStep: Int,
    onPhoneChange: (String) -> Unit,
    onResetCodeChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSendCode: () -> Unit,
    onResetPassword: () -> Unit,
    onBack: () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.arrow_left),
                    contentDescription = "返回",
                    colorFilter = ColorFilter.tint(TextTertiary),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "忘记密码",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (resetStep == 1) {
            Text(
                text = "请输入注册时的手机号或邮箱，我们将发送验证码到您的手机",
                color = TextSecondary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            LoginInputField(
                value = phone,
                onValueChange = onPhoneChange,
                placeholder = "手机号/邮箱",
                iconRes = R.drawable.smartphone,
                isError = phoneError != null,
                errorText = phoneError,
                keyboardType = if (phone.contains("@")) KeyboardType.Email else KeyboardType.Phone
            )
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(Primary)
                    .clickable(onClick = onSendCode)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "发送验证码",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Text(
                text = "请输入验证码和新密码",
                color = TextSecondary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            LoginInputField(
                value = resetCode,
                onValueChange = onResetCodeChange,
                placeholder = "验证码",
                iconRes = R.drawable.lock,
                isError = resetCodeError != null,
                errorText = resetCodeError,
                keyboardType = KeyboardType.Number
            )
            Spacer(modifier = Modifier.height(12.dp))
            LoginInputField(
                value = password,
                onValueChange = onPasswordChange,
                placeholder = "新密码（至少6位）",
                iconRes = R.drawable.lock,
                isError = passwordError != null,
                errorText = passwordError,
                isPassword = true,
                keyboardType = KeyboardType.Password
            )
            Spacer(modifier = Modifier.height(12.dp))
            LoginInputField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                placeholder = "确认密码",
                iconRes = R.drawable.lock,
                isError = confirmPasswordError != null,
                errorText = confirmPasswordError,
                isPassword = true,
                keyboardType = KeyboardType.Password
            )
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(Primary)
                    .clickable(onClick = onResetPassword)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "重置密码",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
