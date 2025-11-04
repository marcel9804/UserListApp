package com.example.userlistapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnFetch: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var queue: RequestQueue

    private val apiUrl = "https://jsonplaceholder.typicode.com/users"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        btnFetch = findViewById(R.id.btnFetch)
        progressBar = findViewById(R.id.progressBar)

        queue = Volley.newRequestQueue(this)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = UserAdapter(emptyList()) { /* no-op initially */ }

        btnFetch.setOnClickListener {
            fetchUsers()
        }
    }

    private fun fetchUsers() {
        progressBar.visibility = View.VISIBLE
        btnFetch.isEnabled = false

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, apiUrl, null,
            { response ->
                val users = mutableListOf<User>()
                try {
                    for (i in 0 until response.length()) {
                        val obj = response.getJSONObject(i)
                        val id = obj.optInt("id")
                        val name = obj.optString("name", "N/A")
                        val email = obj.optString("email", "N/A")
                        val phone = obj.optString("phone", "N/A")
                        users.add(User(id, name, email, phone))
                    }

                    // set adapter with data and click listener
                    recyclerView.adapter = UserAdapter(users) { user ->
                        Toast.makeText(this, "You selected ${user.name}", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed to parse data.", Toast.LENGTH_SHORT).show()
                } finally {
                    progressBar.visibility = View.GONE
                    btnFetch.isEnabled = true
                }
            },
            { error ->
                error.printStackTrace()
                progressBar.visibility = View.GONE
                btnFetch.isEnabled = true
                Toast.makeText(this, "Failed to fetch data.", Toast.LENGTH_SHORT).show()
            }
        )

        // optional: set a timeout / retry policy if needed
        queue.add(jsonArrayRequest)
    }

    override fun onStop() {
        super.onStop()
        queue.cancelAll { true }
    }
}