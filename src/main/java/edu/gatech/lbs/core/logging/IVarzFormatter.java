// Copyright (c) 2012, Georgia Tech Research Corporation
// Authors:
//   Peter Pesti (pesti@gatech.edu)
//
package edu.gatech.lbs.core.logging;

import java.io.BufferedWriter;
import java.io.IOException;

public interface IVarzFormatter {
	public void writeTo(BufferedWriter out) throws IOException;
}
