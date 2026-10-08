package com.example.kitchenpalapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kitchenpalapp.R
import com.example.kitchenpalapp.data.Recipe
import com.example.kitchenpalapp.ui.theme.Primary
import com.example.kitchenpalapp.ui.theme.WarmGray
import com.example.kitchenpalapp.ui.theme.WarmOrangeLighter

@Composable
fun RecipeCard(
    recipe: Recipe,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavorite: () -> Unit = {},
    modifier: Modifier = Modifier,
    isHorizontal: Boolean = false,
    /** 当前列表的排序维度；非默认时在卡片上高亮对应指标，让排序依据可见。 */
    sortBadge: String? = null
) {
    val tags = recipe.tags
    Surface(
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        if (isHorizontal) {
            // 固定高度：避免父级高度受限时，内部 Spacer(weight(1f)) 把卡片撑满整块空间
            Row(
                modifier = Modifier.fillMaxWidth().height(90.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(90.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Image(
                        painter = rememberRecipePainter(recipe.imageRes, recipe.image),
                        contentDescription = recipe.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = recipe.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.clock),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(WarmGray),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${recipe.cookTimeMin}分钟",
                            fontSize = 12.sp,
                            color = WarmGray,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Primary.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = recipe.difficulty.replace("难度", ""),
                                fontSize = 11.sp,
                                color = Primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        // 排序维度徽章：当前按什么排，就把该项指标高亮出来，避免"排了但看不出来"
                        sortBadge?.let { badge ->
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Primary)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badge,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .padding(end = 12.dp, start = 4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                        .size(28.dp)
                        .clickable(onClick = onFavorite),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = if (isFavorite) R.drawable.heart_filled else R.drawable.heart),
                        contentDescription = "收藏",
                        colorFilter = ColorFilter.tint(if (isFavorite) Color(0xFFD94E4E) else WarmGray),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                    Image(
                        painter = rememberRecipePainter(recipe.imageRes, recipe.image),
                        contentDescription = recipe.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                                    startY = 80f
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .align(Alignment.TopStart),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        tags.firstOrNull()?.let { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Primary.copy(alpha = 0.9f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = tag, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                                .size(28.dp)
                                .clickable(onClick = onFavorite),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = if (isFavorite) R.drawable.heart_filled else R.drawable.heart),
                                contentDescription = "收藏",
                                colorFilter = ColorFilter.tint(if (isFavorite) Color(0xFFD94E4E) else WarmGray),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = recipe.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = recipe.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.star_filled),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(Primary),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "%.1f".format(recipe.rating),
                            fontSize = 12.sp,
                            color = Primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Image(
                            painter = painterResource(id = R.drawable.clock),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(WarmGray),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${recipe.cookTimeMin}分钟",
                            fontSize = 12.sp,
                            color = WarmGray,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
