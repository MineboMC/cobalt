package net.minebo.cobalt.skin;

import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CachedSkin {
   private final String name;
   private final String value;
   private final String signature;

   public int hashCode() {
      return this.name.hashCode();
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof CachedSkin skin)) {
         return false;
      } else {
         return skin.getName().equals(this.name) && skin.getValue().equals(this.value) && skin.getSignature().equals(this.signature);
      }
   }

}
