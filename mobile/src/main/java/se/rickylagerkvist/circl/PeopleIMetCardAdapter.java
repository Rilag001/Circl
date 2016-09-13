package se.rickylagerkvist.circl;

import android.app.Activity;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.Query;

import java.util.Date;

import se.rickylagerkvist.circl.Data.PersonIMet;

/**
 * Created by Ricky on 2016-09-12.
 */
public class PeopleIMetCardAdapter extends FirebaseListAdapter<PersonIMet> {

    public PeopleIMetCardAdapter(Activity activity, Class<PersonIMet> modelClass, int modelLayout, Query ref) {
        super(activity, modelClass, modelLayout, ref);
        this.mActivity = activity;
    }

    @Override
    protected void populateView(View v, PersonIMet model, int position) {

        TextView name = (TextView) v.findViewById(R.id.name);
        TextView place = (TextView) v.findViewById(R.id.whereWeMet);
        TextView time = (TextView) v.findViewById(R.id.timeWeMet);
        ImageView contactPic = (ImageView) v.findViewById(R.id.profile_pic);


        // set name, place and pic
        name.setText(model.getName());
        place.setText(model.getAddress());
        time.setText(Utils.SIMPLE_DATE_FORMAT.format(
                new Date(model.getTimestampLastChangedLong())
        ));
        Glide.with(mActivity).load(Uri.parse(model.getPhotoUri().replace("s96-c", "s75-c"))).into(contactPic);

    }
}

