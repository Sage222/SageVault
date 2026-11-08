package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.BarcodeFormatType
import com.example.util.BarcodeGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context and generate barcode`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SageVault", appName)

    val qrBitmap = BarcodeGenerator.generateBarcodeBitmap("SAGE-VAULT-123", BarcodeFormatType.QR_CODE)
    assertNotNull(qrBitmap)

    val code128Bitmap = BarcodeGenerator.generateBarcodeBitmap("290418849201", BarcodeFormatType.CODE_128)
    assertNotNull(code128Bitmap)
  }
}
