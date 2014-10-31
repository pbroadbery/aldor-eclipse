package aldor.util;

import java.util.List;

import com.google.common.base.Objects;

public class Lists2 {

	public static <T> boolean containsSubList(List<T> list1, List<? extends T> list2) {
		int idx = 0;
		if (list2.isEmpty())
			return true;
		if (list2.size() > list1.size())
			return false;
		
		for (T elt: list1) {
			if (Objects.equal(elt, list2.get(idx))) {
				idx++;
				if (idx == list2.size())
					return true;
			}
		}
		return false;
	}

}
