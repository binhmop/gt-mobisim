// Copyright (c) 2009, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.query;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public abstract class LocationBasedQuery {
  // TODO: move these two out?
  protected QueryKey key; // this is NOT the key of the query in the simulation, but dependent on the client implementation

  protected List<Integer> results; // user IDs of clients satisfying the query

  public void setKey(QueryKey key) {
    if (this.key == null) {
      this.key = key;
    }
  }

  public QueryKey getKey() {
    return key;
  }

  public List<Integer> getResults() {
    return results;
  }

  public void setResults(List<Integer> newResults) {
    results = newResults;
  }

  public void addResults(List<Integer> newResults) {
    results.addAll(newResults);
  }

  public void addResult(Integer result) {
    results.add(result);
  }

  public void removeResult(Integer result) {
    results.remove(result);
  }

  public abstract byte getTypeCode();

  public abstract void saveTo(DataOutputStream out) throws IOException;

  public abstract LocationBasedQuery clone();

}
