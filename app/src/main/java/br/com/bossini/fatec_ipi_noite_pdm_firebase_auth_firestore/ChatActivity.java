package br.com.bossini.fatec_ipi_noite_pdm_firebase_auth_firestore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestFutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView mensagensRecyclerView;
    private ChatAdapter adapter;
    private List <Mensagem> mensagens;
    private FirebaseUser fireUser;
    private CollectionReference collMensagensReference;

    private EditText mensagemEditText;

    private void setupFirebase (){
        fireUser = FirebaseAuth.getInstance().getCurrentUser();
        collMensagensReference =
                FirebaseFirestore.
                        getInstance().
                        collection("mensagens");

        collMensagensReference.addSnapshotListener((result, e) -> {
           mensagens.clear();
           for (DocumentSnapshot doc : result.getDocuments()){
               Mensagem m = doc.toObject(Mensagem.class);
               mensagens.add(m);
           }
           Collections.sort(mensagens);
           adapter.notifyDataSetChanged();
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        setupFirebase();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mensagemEditText =
                findViewById(R.id.mensagemEditText);
        mensagensRecyclerView =
                findViewById(R.id.mensagensRecyclerView);
        mensagens = new ArrayList<>();
        adapter = new ChatAdapter(this, mensagens);
        LinearLayoutManager llm =
                new LinearLayoutManager(this);
        mensagensRecyclerView.setAdapter(adapter);
        mensagensRecyclerView.setLayoutManager(llm);
    }

    public void enviarMensagem(View view) {
        String texto =
                mensagemEditText.getText().toString();
        Mensagem m =
                new Mensagem (texto,
                        new java.util.Date(), fireUser.getEmail());
        collMensagensReference.add(m);
        mensagemEditText.setText("");
    }
}
class ChatViewHolder extends RecyclerView.ViewHolder{
    public TextView dataNomeTextView;
    public TextView mensagemTextView;
    public ImageView profilePicImageView;
    public ChatViewHolder (View raiz){
        super (raiz);
        dataNomeTextView =
                raiz.findViewById(R.id.dataNomeTextView);
        mensagemTextView =
                raiz.findViewById(R.id.mensagemTextView);
        profilePicImageView =
                raiz.findViewById(R.id.profilePicImageView);
    }
}

class ChatAdapter extends RecyclerView.Adapter <ChatViewHolder>{

    private Context context;
    private List <Mensagem> mensagens;
    private Map <String, Bitmap> fotos;

    public ChatAdapter(Context context, List<Mensagem> mensagens){
        this.context = context;
        this.mensagens = mensagens;
        fotos = new HashMap<>();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //return null;
        LayoutInflater inflater = LayoutInflater.from(context);
        View raiz = inflater.inflate(
            R.layout.list_item,
            parent,
            false
        );
        return new ChatViewHolder(raiz);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Mensagem m = mensagens.get(position);
        holder.dataNomeTextView.setText(
                context.getString(
                        R.string.data_nome,
                        DateHelper.format(m.getData()),
                        m.getEmail()
                )
        );
        holder.mensagemTextView.setText(m.getTexto());

        StorageReference pictureStorageReference =
                FirebaseStorage.getInstance().getReference(
                    String.format(
                            Locale.getDefault(),
                            "images/%s/profilePic.jpg",
                            m.getEmail().replace("@", "")
                    )
                );

        if (fotos.containsKey(m.getEmail())){
            holder.profilePicImageView.setImageBitmap(
                    fotos.get(m.getEmail())
            );
        }
        else{
            pictureStorageReference.getDownloadUrl()
                    .addOnSuccessListener(
                            (result) ->{
                                Glide.
                                        with(context).
                                        asBitmap().addListener(new RequestListener<Bitmap>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                        fotos.put(m.getEmail(), resource);
                                        holder.profilePicImageView.setImageBitmap(resource);
                                        return true;
                                    }
                                }).
                                        load(pictureStorageReference).
                                        into(holder.profilePicImageView);
                            }
                    )
                    .addOnFailureListener(
                            (exception) -> {
                                holder.profilePicImageView.setImageResource(
                                        R.drawable.ic_person_black_50dp
                                );
                            }
                    );

        }

    }

    @Override
    public int getItemCount() {
        return mensagens.size();
    }
}

