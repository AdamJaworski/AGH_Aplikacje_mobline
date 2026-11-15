package com.example.apkmoblineprojekt.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.apkmoblineprojekt.location.GeocodedPlace
import com.example.apkmoblineprojekt.location.GeocodingRepository
import kotlinx.coroutines.launch

@Composable
fun PlaceSearchField(
    onResolved: (GeocodedPlace) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current.applicationContext
    val geocoder = remember { GeocodingRepository(context) }
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    OutlinedTextField(
        value = query,
        onValueChange = { query = it; error = null },
        modifier = modifier,
        label = { Text("Adres lub nazwa miejsca") },
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            if (loading) return@KeyboardActions
            scope.launch {
                loading = true
                val result = geocoder.geocode(query)
                loading = false
                if (result != null) onResolved(result) else error = "Nie znaleziono adresu"
            }
        }),
        trailingIcon = {
            if (loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
        },
    )
}