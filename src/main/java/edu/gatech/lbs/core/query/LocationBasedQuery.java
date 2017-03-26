// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.query;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

public abstract class LocationBasedQuery {
  protected QueryKey key; // this is NOT the key of the query in the simulation, but dependent on the client implementation
  protected HashSet<Integer> results; // userId of clients satisfying the query

  public void setKey(QueryKey key) {
    if (this.key == null) {
      this.key = key;
    }
  }

  public QueryKey getKey() {
    return key;
  }

  public HashSet<Integer> getResults() {
    return results;
  }

  public int getResultCount() {
    return results == null ? 0 : results.size();
  }

  public void setResults(Collection<Integer> newResults) {
    results = new HashSet<Integer>(newResults);
  }

  public boolean addResult(Integer result) {
    return results.add(result);
  }

  public void addResults(Collection<Integer> newResults) {
    if (results == null) {
      setResults(newResults);
    } else {
      results.addAll(newResults);
    }
  }

  public boolean removeResult(Integer result) {
    return results.remove(result);
  }

  public void removeResults(Collection<Integer> obsoleteResults) {
    results.removeAll(obsoleteResults);
  }

  public abstract byte getTypeCode();

  public abstract void saveTo(DataOutputStream out) throws IOException;

  public abstract LocationBasedQuery clone();

}
