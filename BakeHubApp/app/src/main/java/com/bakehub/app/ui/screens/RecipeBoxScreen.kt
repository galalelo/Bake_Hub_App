package com.bakehub.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bakehub.app.data.MockData
import com.bakehub.app.data.Recipe
import com.bakehub.app.ui.components.RecipeListRow
import com.bakehub.app.ui.theme.Cream
import com.bakehub.app.ui.theme.DarkBrown
import com.bakehub.app.ui.theme.GreyText
import com.bakehub.app.ui.theme.Terracotta

private val tabs = listOf("All", "Saved", "My Recipes", "Offline")

/**
 * 3.7 Recipe Box — manage saved/created recipes and offline availability.
 * Navigates to: Recipe Detail, Bake Mode, Create Recipe.
 */
@Composable
fun RecipeBoxScreen(onOpenRecipe: (Recipe) -> Unit, onCreateRecipe: () -> Unit) {
    var tabIndex by remember { mutableIntStateOf(0) }

    val savedRecipes = MockData.recipes.filter { MockData.savedRecipeIds.contains(it.id) }
    val myRecipes = MockData.recipes.filter { it.source == "User" }
    val offlineRecipes = MockData.recipes.filter { MockData.offlineRecipeIds.contains(it.id) }

    val shown = when (tabIndex) {
        1 -> savedRecipes
        2 -> myRecipes
        3 -> offlineRecipes
        else -> (savedRecipes + myRecipes).distinctBy { it.id }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text("My Recipe Box", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = DarkBrown)
        Text(
            "${savedRecipes.size} saved \u00b7 ${offlineRecipes.size} available offline",
            color = GreyText,
            modifier = Modifier.padding(top = 2.dp, bottom = 16.dp),
        )
        Text(
            "+ New recipe",
            color = Terracotta,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable(onClick = onCreateRecipe)
                .padding(bottom = 16.dp),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            tabs.forEachIndexed { index, label ->
                Text(
                    label,
                    color = if (index == tabIndex) Terracotta else GreyText,
                    fontWeight = if (index == tabIndex) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.clickable { tabIndex = index },
                )
            }
        }

        LazyColumn(
            modifier = Modifier.padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 90.dp),
        ) {
            items(shown) { recipe ->
                val offlineLabel = if (MockData.offlineRecipeIds.contains(recipe.id)) "\u2b07 Available offline" else "\u2601 Online only"
                RecipeListRow(recipe = recipe, trailingLabel = offlineLabel, onClick = { onOpenRecipe(recipe) })
            }
        }
    }
}
