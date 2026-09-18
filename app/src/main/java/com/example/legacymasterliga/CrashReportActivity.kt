package com.example.legacymasterliga

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class CrashReportActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("crash_reports", Context.MODE_PRIVATE)
        val crashText = prefs.getString("last_crash", "Nenhum erro registrado.") ?: ""

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            setBackgroundColor(Color.parseColor("#0A1428"))
        }

        val title = TextView(this).apply {
            text = "⚠️ Erro técnico do app"
            setTextColor(Color.WHITE)
            textSize = 20f
            setPadding(0, 0, 0, 32)
        }

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        }
        val errorText = TextView(this).apply {
            text = crashText
            setTextColor(Color.parseColor("#00E6C8"))
            setTextIsSelectable(true)
            textSize = 11f
        }
        scrollView.addView(errorText)

        val copyButton = Button(this).apply {
            text = "Copiar Erro"
            setOnClickListener {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("crash", crashText))
                Toast.makeText(this@CrashReportActivity, "Copiado!", Toast.LENGTH_SHORT).show()
            }
        }

        val continueButton = Button(this).apply {
            text = "Continuar (limpar erro)"
            setOnClickListener {
                prefs.edit().remove("last_crash").commit()
                finish()
            }
        }

        layout.addView(title)
        layout.addView(scrollView)
        layout.addView(copyButton)
        layout.addView(continueButton)
        setContentView(layout)
    }
}
