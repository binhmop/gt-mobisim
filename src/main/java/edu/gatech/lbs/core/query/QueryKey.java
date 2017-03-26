// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.query;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

//
public class QueryKey {
  public int uid; // user-ID, unique in the entire system
  public int qid; // query-ID, unique for the user only

  public QueryKey(int uid, int qid) {
    this.uid = uid;
    this.qid = qid;
  }

  public QueryKey(DataInputStream in) throws IOException {
    uid = in.readInt();
    qid = in.readInt();
  }

  public QueryKey clone() {
    return new QueryKey(uid, qid);
  }

  public boolean equals(Object o) {
    if (o instanceof QueryKey) {
      QueryKey oo = (QueryKey) o;
      return (uid == oo.uid && qid == oo.qid);
    }
    return false;
  }

  public int hashCode() {
    return new Integer(uid | qid << 24).hashCode();
  }

  public void saveTo(DataOutputStream out) throws IOException {
    out.writeInt(uid);
    out.writeInt(qid);
  }

  public String toString() {
    return uid + ":" + qid;
  }
}
