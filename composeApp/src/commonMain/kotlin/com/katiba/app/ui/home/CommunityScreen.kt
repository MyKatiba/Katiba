package com.katiba.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.katiba.app.ui.theme.KatibaColors

@Composable
fun CommunityScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F0F0)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Friends", color = Color.Black)
                }
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F0F0)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Friends", color = Color.Black)
                }
            }
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(sampleFriends) { friend ->
                    FriendSuggestionCard(friend)
                }
            }
        }

        item {
            SocialActivityCard()
        }
    }
}

data class Friend(val name: String, val subtitle: String, val initials: String)

val sampleFriends = listOf(
    Friend("Mark Lean", "Contact", "ML"),
    Friend("Cecil Don", "1 Mutual Friend", "CD"),
    Friend("Stephen K...", "1 Mutual Friend", "SK"),
    Friend("Lily", "1 Mutual Friend", "L")
)

@Composable
fun FriendSuggestionCard(friend: Friend) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text(friend.initials, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0F0F0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(friend.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(friend.subtitle, fontSize = 10.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {},
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.height(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F0F0)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Add Friend", fontSize = 10.sp, color = Color.Black)
        }
    }
}

@Composable
fun SocialActivityCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("E")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Essy Kimani earned the Saved Verse Badge",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Text("1w", fontSize = 12.sp, color = Color.Gray)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0F0F0)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = KatibaColors.KenyaRed
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("1", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = "Like")
                Icon(Icons.Default.MailOutline, contentDescription = "Comment")
            }
        }
    }
}
