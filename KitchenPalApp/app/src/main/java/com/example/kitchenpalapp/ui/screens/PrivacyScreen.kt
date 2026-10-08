package com.example.kitchenpalapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kitchenpalapp.R
import com.example.kitchenpalapp.data.KitchenPalRepository
import com.example.kitchenpalapp.ui.theme.Background
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.TextPrimary
import com.example.kitchenpalapp.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val defaultPrivacyContent = """
隐私政策

更新日期：2026年1月1日

KitchenPal（以下简称"我们"）深知个人信息对您的重要性，并会尽全力保护您的个人信息安全可靠。我们致力于维持您对我们的信任，恪守以下原则，保护您的个人信息：权责一致原则、目的明确原则、选择同意原则、最少够用原则、确保安全原则、主体参与原则、公开透明原则等。同时，我们承诺，我们将按业界成熟的安全标准，采取相应的安全保护措施来保护您的个人信息。

一、我们如何收集和使用您的个人信息

个人信息是指以电子或者其他方式记录的能够单独或者与其他信息结合识别特定自然人身份或者反映特定自然人活动情况的各种信息。我们仅会出于本政策所述的以下目的，收集和使用您的个人信息：

（一）帮助您成为我们的用户

为便于我们为您提供服务，您需要提供以下信息：
1. 手机号码：用于注册、登录账号及账号安全验证
2. 昵称和头像：用于展示您的个人资料
3. 密码：用于保护您的账号安全

（二）向您提供服务

1. 食谱推荐服务
我们会根据您的口味偏好、浏览历史、收藏记录等信息，为您推荐个性化的食谱内容。

2. 食材识别服务
当您使用拍照识别食材功能时，我们会收集您拍摄的照片，用于食材识别分析。这些照片仅用于识别目的，识别完成后会在合理时间内删除。

3. 烹饪监控服务
当您使用烹饪计时和火候监控功能时，我们会收集您的烹饪时长、菜品类型等信息，用于优化烹饪建议和提供历史记录。

4. 收藏与历史记录
我们会保存您收藏的食谱和烹饪历史记录，以便您随时查看和管理。

（三）设备信息与日志

为了保障软件安全运行和优化服务体验，我们可能会收集您的设备信息，包括：
- 设备型号、操作系统版本
- 设备标识符（如 Android ID）
- IP 地址
- 应用版本号

二、我们如何使用 Cookie 和同类技术

为确保网站正常运转、为您获得更轻松的访问体验，我们可能会在您的设备上存储名为 Cookie 的小数据文件。Cookie 通常包含标识符、站点名称以及一些号码和字符。借助于 Cookie，网站能够存储您的偏好数据。

三、我们如何共享、转让、公开披露您的个人信息

（一）共享

我们不会与任何公司、组织和个人分享您的个人信息，但以下情况除外：
1. 在获取明确同意的情况下共享
2. 我们可能会根据法律法规规定，或按政府主管部门的强制性要求，对外共享您的个人信息

（二）转让

我们不会将您的个人信息转让给任何公司、组织和个人。

（三）公开披露

我们仅会在以下情况下，公开披露您的个人信息：
1. 获得您的明确同意
2. 基于法律的披露：在法律、法律程序、诉讼或政府主管部门强制性要求的情况下

四、我们如何保护您的个人信息

我们已使用符合业界标准的安全防护措施保护您提供的个人信息，防止数据遭到未经授权访问、公开披露、使用、修改、损坏或丢失。我们会采取一切合理可行的措施，保护您的个人信息。

五、您的权利

按照中国相关的法律、法规、标准，以及其他国家、地区的通行做法，我们保障您对自己的个人信息行使以下权利：
1. 访问您的个人信息
2. 更正您的个人信息
3. 删除您的个人信息
4. 改变您授权同意的范围
5. 注销您的账号

六、未成年人保护

我们非常重视对未成年人个人信息的保护。根据相关法律法规的规定，若您是 18 周岁以下的未成年人，在使用我们的服务前，应事先取得您的家长或法定监护人的同意。

七、隐私政策的更新

我们可能适时修订本隐私政策的条款，该等修订构成本隐私政策的一部分。如该等修订造成您在本隐私政策下权利的实质减少，我们将在修订生效前通过在主页上显著位置提示或向您发送电子邮件或其他适当方式通知您。

八、如何联系我们

如果您对本隐私政策有任何疑问、意见或建议，通过以下方式与我们联系：
客服邮箱：privacy@kitchenpal.com
客服电话：400-888-8888

一般情况下，我们将在十五个工作日内回复。
""".trimIndent()

@Composable
fun PrivacyScreen(
    onBack: () -> Unit
) {
    var content by remember { mutableStateOf(defaultPrivacyContent) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val result = KitchenPalRepository.getAgreement("privacy")
                if (result.isSuccess && result.data != null) {
                    val text = result.data["content"] as? String
                    if (!text.isNullOrBlank()) {
                        content = text
                    }
                }
            } catch (_: Exception) {
            }
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        PrivacyTopBar(onBack = onBack)
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = content,
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PrivacyTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Image(
                painter = painterResource(id = R.drawable.arrow_left),
                contentDescription = "返回",
                colorFilter = ColorFilter.tint(TextSecondary),
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = "隐私政策",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(48.dp))
    }
}
