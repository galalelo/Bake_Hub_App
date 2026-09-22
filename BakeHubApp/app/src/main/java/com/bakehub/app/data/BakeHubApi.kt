package com.bakehub.app.data

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.bakehub.app.BuildConfig

object BakeHubApi {
    private const val TAG = "BakeHubApi"
    // For a local XAMPP emulator use http://10.0.2.2/bakehub-api/api.php.
    // Replace this value with the public HTTPS URL after hosting the PHP API.
    private val baseUrl: String get() = BuildConfig.BAKEHUB_API_URL.trimEnd('/')

    private suspend fun request(path: String, method: String = "GET", payload: JSONObject? = null): JSONObject = withContext(Dispatchers.IO) {
        val connection = (URL("$baseUrl$path").openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 6000
            readTimeout = 8000
            setRequestProperty("Accept", "application/json")
            if (payload != null) { doOutput = true; setRequestProperty("Content-Type", "application/json") }
        }
        try {
            payload?.toString()?.toByteArray()?.let { connection.outputStream.use { stream -> stream.write(it) } }
            val text = (if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream).bufferedReader().use { it.readText() }
            if (connection.responseCode !in 200..299) error(text)
            JSONObject(text)
        } finally { connection.disconnect() }
    }

    suspend fun login(email: String, password: String): Boolean = runCatching {
        request("/auth/login", "POST", JSONObject().put("email", email).put("password", password)); true
    }.onFailure { Log.w(TAG, "Login API unavailable; using prototype fallback", it) }.getOrDefault(false)

    suspend fun searchRecipes(query: String): List<Recipe> = runCatching {
        val json = request("/recipes?search=" + java.net.URLEncoder.encode(query, "UTF-8"))
        json.getJSONArray("recipes").toRecipeList()
    }.onFailure { Log.w(TAG, "Recipe API unavailable; using mock data", it) }.getOrDefault(emptyList())

    suspend fun publishRecipe(title: String, ingredients: List<String>, steps: List<String>): Boolean = runCatching {
        val ingredientJson = JSONArray().apply { ingredients.filter { it.isNotBlank() }.forEach { put(JSONObject().put("name", it)) } }
        val stepJson = JSONArray().apply { steps.filter { it.isNotBlank() }.forEach { put(it) } }
        request("/recipes", "POST", JSONObject().put("title", title).put("ingredients", ingredientJson).put("steps", stepJson)); true
    }.onFailure { Log.w(TAG, "Publish API unavailable; keeping prototype navigation", it) }.getOrDefault(false)

    private fun JSONArray.toRecipeList(): List<Recipe> = (0 until length()).mapNotNull { getJSONObject(it).toRecipe() }
    private fun JSONObject.toRecipe(): Recipe? = runCatching {
        val n = optJSONObject("nutrition") ?: JSONObject()
        Recipe(
            id = getString("id"), title = getString("title"), authorName = optString("author_name", "BakeHub"), source = optString("source", "BakeHub"),
            category = optString("category", "Other"), difficulty = runCatching { Difficulty.valueOf(optString("difficulty", "EASY")) }.getOrDefault(Difficulty.EASY),
            prepTimeMinutes = optInt("prep_time_minutes"), cookTimeMinutes = optInt("cook_time_minutes"), servings = optInt("servings", 1), avgRating = optDouble("avg_rating").toFloat(), ratingCount = optInt("rating_count"),
            nutrition = Nutrition(n.optInt("calories"), n.optDouble("proteinG").toFloat(), n.optDouble("carbsG").toFloat(), n.optDouble("fatG").toFloat()),
            ingredients = emptyList(), steps = emptyList()
        )
    }.getOrNull()
}
