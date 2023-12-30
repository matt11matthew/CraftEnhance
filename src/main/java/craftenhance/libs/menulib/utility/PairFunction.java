package craftenhance.libs.menulib.utility;

import craftenhance.libs.menulib.utility.Item.Pair;
import javax.annotation.Nonnull;

public interface PairFunction<T> {
   @Nonnull
   Pair<T, Boolean> apply();
}
