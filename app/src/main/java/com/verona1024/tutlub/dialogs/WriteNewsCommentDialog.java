package com.verona1024.tutlub.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.okhttp.ResponseBody;
import com.verona1024.tutlub.R;
import com.verona1024.tutlub.TutLubProvider;
import com.verona1024.tutlub.models.NewsObject;
import com.verona1024.tutlub.utils.RequestUtil;
import com.verona1024.tutlub.utils.UserUtil;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import static android.app.Activity.RESULT_OK;

public class WriteNewsCommentDialog extends DialogFragment {
    private static final String POST = "post";
    private static final int RESULT_LOAD_IMAGE = 0;

    private ImageLoader imgloader = ImageLoader.getInstance();
    ImageView imageView;
    Bitmap bitmap;

    public static WriteNewsCommentDialog newInstance(NewsObject newsObject) {

        Bundle args = new Bundle();
        args.putParcelable(POST, newsObject);

        WriteNewsCommentDialog fragment = new WriteNewsCommentDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public interface NoticeDialogListener {
        void onDialogShareClick(int comments);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            bitmap = BitmapFactory.decodeFile(picturePath);

            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(bitmap);

        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final NewsObject commentObject = getArguments().getParcelable(POST);

        View view = inflater.inflate(R.layout.dialog_write_comment, null);
        imgloader.displayImage(UserUtil.picture, (ImageView) view.findViewById(R.id.imageViewProfile));
        ((TextView) view.findViewById(R.id.textViewName)).setText(UserUtil.name);

        final EditText text = (EditText) view.findViewById(R.id.editTextText);

        view.findViewById(R.id.textViewCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        view.findViewById(R.id.buttonShare).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!text.getText().toString().trim().isEmpty()) {

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(TutLubProvider.BASE_URL)
                            .build();
                    TutLubProvider trackerInternetProvider = retrofit.create(TutLubProvider.class);

                    Call<ResponseBody> call =  trackerInternetProvider.commentNews(commentObject.postId, text.getText().toString(), RequestUtil.OAUTH_HEADER);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
                            if (getActivity() instanceof NoticeDialogListener){
                                ((NoticeDialogListener) getActivity()).onDialogShareClick(++commentObject.number_comments);
                            }
                            dismiss();
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            t.printStackTrace();
                        }
                    });
                } else {
                    DialogFragment newFragment = MessageDialog.newInstance("Please, put some text");
                    newFragment.show(getFragmentManager().beginTransaction(), "dialog");
                }
            }
        });

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view);
        return builder.create();
    }
}
