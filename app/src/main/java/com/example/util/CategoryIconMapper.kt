package com.example.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryIconOption(
  val key: String,
  val label: String,
  val icon: ImageVector
)

object CategoryIconMapper {
  val iconOptions = listOf(
    CategoryIconOption("library", "Library", Icons.Filled.LocalLibrary),
    CategoryIconOption("gym", "Gym & Fitness", Icons.Filled.FitnessCenter),
    CategoryIconOption("insurance", "Insurance", Icons.Filled.HealthAndSafety),
    CategoryIconOption("medical", "Medical", Icons.Filled.LocalHospital),
    CategoryIconOption("membership", "Membership", Icons.Filled.WorkspacePremium),
    CategoryIconOption("shopping", "Retail & Loyalty", Icons.Filled.ShoppingBag),
    CategoryIconOption("transit", "Transit", Icons.Filled.DirectionsBus),
    CategoryIconOption("id_badge", "ID & Access", Icons.Filled.Badge),
    CategoryIconOption("ticket", "Events & Passes", Icons.Filled.ConfirmationNumber),
    CategoryIconOption("civic", "Civic & Museum", Icons.Filled.AccountBalance),
    CategoryIconOption("card", "General Card", Icons.Filled.CreditCard),
    CategoryIconOption("custom", "Custom Vault", Icons.Filled.FolderSpecial)
  )

  fun getIcon(iconName: String): ImageVector {
    return iconOptions.find { it.key == iconName }?.icon ?: Icons.Filled.CreditCard
  }
}
