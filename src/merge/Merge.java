package merge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import cn.cnic.peer.entity.Piece;
import cn.cnic.peer.main.Main;

public class Merge {
	public class PieceCmp implements Comparator<Piece> {

		public int compare(Piece p1, Piece p2) {
			if (p1.getOffset() < p2.getOffset()) {
				return -1;
			}
			if (p1.getOffset() == p2.getOffset() && p1.getOffset() + p1.getLength() <= p2.getOffset() + p2.getLength()) {
				return -1;
			}
			return 1;
		}
	}

	public List<Piece> merge(List<Piece> pieces) {
		List<Piece> ret = new ArrayList<Piece>();
		if (pieces.size() == 0) {
			return ret;
		}
		Piece[] arr = new Piece[pieces.size()];
		pieces.toArray(arr);
		Arrays.sort(arr, new PieceCmp());
		int start = arr[0].getOffset();
		int end = arr[0].getOffset() + arr[0].getLength();
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].getOffset() <= end) {
				end = Math.max(end, arr[i].getOffset() + arr[i].getLength());
			} else {
				Piece p = new Piece();
				p.setOffset(start);
				p.setLength(end - start);
				ret.add(p);
				start = arr[i].getOffset();
				end = arr[i].getOffset() + arr[i].getLength();
			}
		}
		Piece p = new Piece();
		p.setOffset(start);
		p.setLength(end -start);
		ret.add(p);
		return ret;
	}
	
	public List<Piece> mergePeer(List<Piece> pieces) {
		List<Piece> ret = new ArrayList<Piece>();
		if (pieces.size() == 0) {
			return ret;
		}
		Piece[] arr = new Piece[pieces.size()];
		pieces.toArray(arr);
		Arrays.sort(arr, new PieceCmp());
		ret.add(arr[0]);
		int start = arr[0].getOffset();
		int end = arr[0].getOffset() + arr[0].getLength();
		for (int i = 1; i < arr.length; i++) {
			if (arr[i].getOffset() <= end) {
				int tempEnd = arr[i].getLength() + arr[i].getOffset();
				if(tempEnd > end) {
					arr[i].setLength(tempEnd - end);
					arr[i].setOffset(end);
					end = tempEnd;
					ret.add(arr[i]);
				}
			} else {
				start = arr[i].getOffset();
				end = arr[i].getOffset() + arr[i].getLength();
				arr[i].setOffset(start);
				arr[i].setLength(end - start);
				ret.add(arr[i]);
			}
		}
		return ret;
	}
	
	public static void main(String[] args) {
		List<Piece> list = new ArrayList<Piece>();
		Piece p1 = new Piece();
		p1.setContentHash("a");
		p1.setOffset(10);
		p1.setLength(20);
		Piece p2 = new Piece();
		p2.setContentHash("b");
		p2.setOffset(20);
		p2.setLength(20);
		Piece p3 = new Piece();
		p3.setContentHash("c");
		p3.setOffset(50);
		p3.setLength(20);
		Piece p4 = new Piece();
		p4.setContentHash("d");
		p4.setOffset(20);
		p4.setLength(25);
		list.add(p1);
		list.add(p2);
		list.add(p3);
		list.add(p4);
		List<Piece> res = new Merge().mergePeer(list);
		for(Piece r : res) {
			System.out.println(r.getContentHash()+":"+r.getOffset()+"~"+ (r.getOffset()+r.getLength()));
		}
	}
}
