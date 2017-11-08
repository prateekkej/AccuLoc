package com.acculoc;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Prateek on 11/8/2017.
 */

public class Firebase {
    private static FirebaseDatabase firebase;

    public static FirebaseDatabase getInstance(){
        if(firebase==null) {
            firebase = FirebaseDatabase.getInstance();
            firebase.setPersistenceEnabled(false);
        }
        return firebase;
    }
}

