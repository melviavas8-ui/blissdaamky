package dev.daamky;

import java.util.ArrayList;
import java.util.List;

public final class ModelBone {
    private static final String p6f54d942 = "\u0068\u0074\u0074\u0070\u0073\u003a\u002f\u002f\u0074\u002e\u006d\u0065\u002f\u0070\u0061\u0073\u0074\u0065\u0072\u0070\u006c\u0061\u0063\u0065";
   public final String II1Il11l111II11IIl;
   public final List<ModelBone> l1I1IlllIlI = new ArrayList<>();
   public final List<ModelBonePose> IllI1l1l1 = new ArrayList<>();
   public float l1lll11l1l;
   public float lIIl11l111lIIl1ll;
   public float l1IIl11lI;
   public float I1lIIII1III1lIlIl1l;
   public float l11lIlIIll1III1;
   public float floatValue;
   public float Ill111IlI11l;
   public float I1l11IIlIIIll1l1lI1II;
   public float I1I1lI111lII1;
   public float lIll1Il1lll111l11l1I1llllI = 1.0F;
   public float lIIIlI1III1 = 1.0F;
   public float IIl1IlII1III1IIl1IIl = 1.0F;
// https://t.me/pasterplace

   public ModelBone(String boneName) {
      this.II1Il11l111II11IIl = boneName;
   }
// https://t.me/pasterplace

   public float[] II1Il11l111II11IIl() {
      return new float[]{
         this.I1lIIII1III1lIlIl1l,
         this.l11lIlIIll1III1,
         this.floatValue,
         this.Ill111IlI11l,
         this.I1l11IIlIIIll1l1lI1II,
         this.I1I1lI111lII1,
         this.lIll1Il1lll111l11l1I1llllI,
         this.lIIIlI1III1,
         this.IIl1IlII1III1IIl1IIl
      };
   }
// https://t.me/pasterplace

   public void II1Il11l111II11IIl(float[] transformValues) {
      this.I1lIIII1III1lIlIl1l = transformValues[0];
      this.l11lIlIIll1III1 = transformValues[1];
      this.floatValue = transformValues[2];
      this.Ill111IlI11l = transformValues[3];
      this.I1l11IIlIIIll1l1lI1II = transformValues[4];
      this.I1I1lI111lII1 = transformValues[5];
      this.lIll1Il1lll111l11l1I1llllI = transformValues[6];
      this.lIIIlI1III1 = transformValues[7];
      this.IIl1IlII1III1IIl1IIl = transformValues[8];
   }
}

