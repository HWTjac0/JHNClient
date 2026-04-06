package com.example.hackernews_client.ui.theme

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdownMenu(
    options: List<String>,
    isTransparent: Boolean = true,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    onSelected: (Int) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var textValue by remember { mutableStateOf(options[selectedIndex]) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        TextField(
            value = textValue,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                TrailingIcon(expanded = expanded)
            },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
            singleLine = true,
            colors = ExposedDropdownMenuDefaults.textFieldColors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(text = option) },
                    onClick = {
                        textValue = option
                        expanded = false
                        onSelected(index)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
