// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Juntos contributors
package doapp.juntos;
public final class Versions {
 public static int compare(long ar,String ao,long br,String bo){int n=Long.compare(ar,br);return n!=0?n:ao.compareTo(bo);}
}
