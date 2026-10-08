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

private val defaultTermsContent = """
用户协议

欢迎使用 KitchenPal 应用！

更新日期：2026年1月1日

请您仔细阅读以下条款。如果您不同意本协议的任何内容，请不要注册或使用 KitchenPal 服务。您的注册、登录、使用等行为将视为对本协议的接受，并同意接受本协议各项条款的约束。

一、服务条款的确认与接纳

您通过点击"同意"按钮或以其他方式选择接受本协议，即表示您已阅读、理解并同意接受本协议各项条款的约束。如您不同意本协议，请您不要注册、登录或使用本服务。

二、服务内容

1. KitchenPal 是一款智能厨房助手应用，为用户提供以下服务：
   - 食谱浏览与搜索
   - 食材拍照识别
   - 烹饪计时与火候监控
   - 食谱收藏与管理
   - 烹饪历史记录
   - 个性化口味推荐
   - 在线大厨咨询

2. 我们保留随时变更、中断或终止部分或全部服务的权利，无需对用户或第三方承担责任。

三、用户账号

1. 用户在注册时应提供真实、准确、完整的个人资料。
2. 用户应妥善保管自己的账号和密码，因用户保管不当造成损失的，由用户自行承担。
3. 用户不得以任何形式将账号转让、出借给他人使用。
4. 用户发现账号被盗用或存在安全漏洞时，应立即通知我们。

四、用户行为规范

用户使用本服务时，不得从事下列行为：
1. 发布、传播危害国家安全、泄露国家秘密、破坏社会稳定的内容
2. 发布、传播侮辱、诽谤、色情、暴力、粗俗的内容
3. 侵犯他人知识产权、隐私权、名誉权等合法权益
4. 利用本服务从事任何违法犯罪活动
5. 恶意注册账号，或使用任何方式干扰、破坏本服务的正常运行
6. 未经授权访问他人账号、窃取他人数据
7. 其他违反法律法规及社会公德的行为

五、知识产权

1. KitchenPal 应用中的所有内容，包括但不限于文字、图片、音频、视频、软件、程序、版面设计等，均受中华人民共和国著作权法及其他相关法律法规的保护，其知识产权归 KitchenPal 或相关权利人所有。
2. 未经 KitchenPal 书面同意，用户不得以任何形式复制、传播、修改、出版、展示、表演、演示或用于其他商业目的。

六、免责声明

1. 用户明确同意使用本服务的风险由其自行承担。我们不对服务的可用性、及时性、安全性、准确性做任何担保。
2. 因不可抗力或非我们原因导致的服务中断，我们不承担责任。
3. 用户因使用本服务而产生的任何直接或间接损失，我们不承担赔偿责任。
4. 本应用提供的食谱内容仅供参考，实际烹饪请结合自身情况判断。因食材质量、烹饪环境、个人体质等因素导致的任何问题，我们不承担责任。

七、服务的变更、中断或终止

1. 我们有权根据实际情况随时对服务内容进行调整、变更、中断或终止。
2. 如用户违反本协议规定，我们有权暂停或终止为用户提供服务。
3. 服务终止后，用户使用本服务的权利立即终止。

八、隐私保护

我们重视用户隐私保护，将按照隐私政策的规定收集、使用、存储和保护用户的个人信息。隐私政策是本协议不可分割的一部分。

九、协议的修改

我们有权根据需要对本协议进行修改，修改后的协议将在应用内公布。用户继续使用本服务即视为接受修改后的协议。

十、法律适用与争议解决

1. 本协议的订立、执行、解释及争议解决均适用中华人民共和国法律。
2. 因本协议引起的争议，双方应友好协商解决；协商不成的，任何一方均可向我们所在地有管辖权的人民法院提起诉讼。

十一、联系我们

如您对本协议有任何疑问或建议，请通过以下方式联系我们：
客服邮箱：support@kitchenpal.com
客服电话：400-888-8888

感谢您选择 KitchenPal，祝您烹饪愉快！
""".trimIndent()

@Composable
fun TermsScreen(
    onBack: () -> Unit
) {
    var content by remember { mutableStateOf(defaultTermsContent) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val result = KitchenPalRepository.getAgreement("terms")
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
        TermsTopBar(onBack = onBack)
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
private fun TermsTopBar(onBack: () -> Unit) {
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
            text = "用户协议",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(48.dp))
    }
}
