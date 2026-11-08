package com.example.util

import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryIconOption(
  val key: String,
  val label: String,
  val icon: ImageVector
)

object CategoryIconMapper {
  val iconOptions = listOf(
    CategoryIconOption("library", "Library", AppIcons.LocalLibrary),
    CategoryIconOption("gym", "Gym & Fitness", AppIcons.FitnessCenter),
    CategoryIconOption("insurance", "Insurance", AppIcons.HealthAndSafety),
    CategoryIconOption("medical", "Medical", AppIcons.LocalHospital),
    CategoryIconOption("membership", "Membership", AppIcons.WorkspacePremium),
    CategoryIconOption("shopping", "Retail & Loyalty", AppIcons.ShoppingBag),
    CategoryIconOption("transit", "Transit", AppIcons.DirectionsBus),
    CategoryIconOption("id_badge", "ID & Access", AppIcons.Badge),
    CategoryIconOption("ticket", "Events & Passes", AppIcons.ConfirmationNumber),
    CategoryIconOption("civic", "Civic & Museum", AppIcons.AccountBalance),
    CategoryIconOption("card", "General Card", AppIcons.CreditCard),
    CategoryIconOption("custom", "Custom Vault", AppIcons.FolderSpecial)
  )

  fun getIcon(iconName: String): ImageVector {
    return iconOptions.find { it.key == iconName }?.icon ?: AppIcons.CreditCard
  }
}
