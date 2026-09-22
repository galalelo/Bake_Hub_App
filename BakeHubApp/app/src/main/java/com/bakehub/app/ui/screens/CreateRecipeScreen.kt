package com.bakehub.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.bakehub.app.data.BakeHubApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bakehub.app.ui.theme.Cream
import com.bakehub.app.ui.theme.DarkBrown
import com.bakehub.app.ui.theme.GreyText
import com.bakehub.app.ui.theme.Terracotta
import com.bakehub.app.ui.theme.WarmGrey
import com.bakehub.app.ui.theme.White

/**
 * 3.9 Create Recipe — let users contribute their own recipes to BakeHub.
 * Navigates to: Recipe Detail of the new recipe / Recipe Box ("My Recipes") on publish.
 */
@Composable
fun CreateRecipeScreen(onPublished: () -> Unit) {
    var title by remember { mutableStateOf("") }
    val ingredients = remember { mutableStateListOf("") }
    val steps = remember { mutableStateListOf("") }
    val scope = rememberCoroutineScope()
    var isPublishing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text("New Recipe", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = DarkBrown)

        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2.2f)
                .padding(vertical = 16.dp)
                .background(WarmGrey, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("+ Add cover photo", color = DarkBrown)
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Recipe title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors(),
        )

        Text("Ingredients", fontWeight = FontWeight.Bold, color = DarkBrown, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
        ingredients.forEachIndexed { i, value ->
            OutlinedTextField(
                value = value,
                onValueChange = { ingredients[i] = it },
                placeholder = { Text("e.g. 2 cups flour") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = fieldColors(),
            )
        }
        Text(
            "+ Add ingredient",
            color = Terracotta,
            modifier = Modifier
                .padding(bottom = 20.dp)
                .clickable { ingredients.add("") },
        )

        Text("Steps", fontWeight = FontWeight.Bold, color = DarkBrown, modifier = Modifier.padding(bottom = 8.dp))
        steps.forEachIndexed { i, value ->
            OutlinedTextField(
                value = value,
                onValueChange = { steps[i] = it },
                placeholder = { Text("Step ${i + 1}\u2026") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = fieldColors(),
            )
        }
        Text(
            "+ Add step",
            color = Terracotta,
            modifier = Modifier
                .padding(bottom = 28.dp)
                .clickable { steps.add("") },
        )

        Button(
            onClick = {
                isPublishing = true
                scope.launch {
                    BakeHubApi.publishRecipe(title, ingredients.toList(), steps.toList())
                    isPublishing = false
                    onPublished()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Terracotta, contentColor = White),
        ) {
            Text(if (isPublishing) "Publishing…" else "Publish Recipe", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Terracotta,
    unfocusedBorderColor = GreyText,
    focusedContainerColor = White,
    unfocusedContainerColor = White,
)
