package com.example.chatgptapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatgptapp.adapter.MessageAdapter;
import com.example.chatgptapp.databinding.ActivityMainBinding;
import com.example.chatgptapp.model.MessageModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    List<MessageModel> messageModels;
    MessageAdapter adapter;

    String url = "https://api.openai.com/v1/completions";
    String accessToken = "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";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        messageModels = new ArrayList<>();

        // initialize adapter class
        adapter = new MessageAdapter(messageModels);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.smoothScrollToPosition(adapter.getItemCount());

        binding.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String question = binding.editText.getText().toString().trim();
                addToRecyclerView(question , MessageModel.SENT_BY_ME);
                binding.editText.setText("");
                binding.welcomeTxt.setVisibility(View.GONE);

                callApi(question);


            }
        });



    }

    private void addToRecyclerView(String question, String sendBy) {

        messageModels.add(new MessageModel(question , sendBy));
        adapter.notifyDataSetChanged();
        binding.recyclerView.smoothScrollToPosition(adapter.getItemCount());

    }

    public void callApi(String question){

        messageModels.add(new MessageModel("Fetching data" , MessageModel.SENT_BY_GPT));

        JSONObject parametter = new JSONObject();
        try {
            parametter.put("model" , "text-davinci-003");
            parametter.put("prompt" ,question );
            parametter.put("max_tokens"  , 1000);
            parametter.put("temperature" , 0);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, parametter, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    JSONArray jsonArray = response.getJSONArray("choices");
                   String answer =  jsonArray.getJSONObject(0).getString("text");
                   
                   getGptResponse(answer);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                getGptResponse("Failed due to : "+error.toString());
                Log.e("errorMessage", error.toString());

            }
        } ) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String , String> header = new HashMap<>();
                header.put("Authorization" , "Bearer "+ accessToken);

                return  header;

            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);




    }

    private void getGptResponse(String answer) {
        messageModels.remove(messageModels.size()-1);
        addToRecyclerView(answer  , MessageModel.SENT_BY_GPT);
    }


}