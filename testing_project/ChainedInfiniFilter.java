package testing_project;

import java.util.ArrayList;

public class ChainedInfiniFilter extends InfiniFilter {

	ArrayList<InfiniFilter> older_filters;
	InfiniFilter former = null;
	int num_expansions = 0;
	int expansions_of_former = 0;
	
	ChainedInfiniFilter(int power_of_two, int bits_per_entry) {
		super(power_of_two, bits_per_entry);
		older_filters = new ArrayList<InfiniFilter>();
	}
	
	void handle_empty_fingerprint(int bucket_index, QuotientFilter current) {
		int bucket1 = bucket_index;
		int fingerprint = bucket_index >> former.power_of_two_size;
		int slot_mask = (1 << former.power_of_two_size) - 1;
		int slot = bucket1 & slot_mask;
		
		/*print_int_in_binary( bucket1, power_of_two_size + 1);
		print_int_in_binary( slot, former.power_of_two_size);
		print_int_in_binary( fingerprint, former.fingerprintLength);
		System.out.println();*/
		
		//System.out.println("moving void entry from bucket " + bucket_index + " to bucket " + slot + " with fingerprint " + fingerprint + " in secondary IF");
		//print_int_in_binary( fingerprint, former.fingerprintLength);
		
		
		num_existing_entries--;
		former.num_existing_entries++;
		former.insert(fingerprint, slot, false);
		
	}
	
	// The hash function is being computed here for each filter 
	// However, it's not such an expensive function, so it's probably not a performance issue. 
	boolean search(int input) {
		if (super.search(input)) {
			return true;
		}
		if (former != null && former.search(input)) {
			return true;
		}
		
		for (QuotientFilter qf : older_filters) {
			if (qf.search(input)) {
				return true;
			}
		}
		return false;
	}
	
	void expand() {
		if (num_expansions == fingerprintLength - 1) { // first time we create a former filter
			former = new InfiniFilter(power_of_two_size - fingerprintLength + 1, bitPerEntry);
		}
		else if (former != null && expansions_of_former == former.fingerprintLength) { // our former filter is full 
			older_filters.add(former);
			former = new InfiniFilter(power_of_two_size - fingerprintLength + 1, bitPerEntry);
			expansions_of_former = 0; 
		}
		else if (former != null) {  // standard procedure
			former.expand();
			expansions_of_former++;
		}
		super.expand();
		//System.out.println("finished expanding ------------");
		num_expansions++;	
	}
	
	boolean rejuvenate(int key) {
		boolean success = super.rejuvenate(key);
		if (success) {
			return true;
		}
		success = former.delete(key);
		if (success) {
			success = insert(key, false);
			if (!success) {
				System.out.println("failed at rejuvenation");
				System.exit(1);
			}
			return true;
		}
		for (int i = older_filters.size() - 1; i >= 0; i--) {						
			success = older_filters.get(i).delete(key);
			if (success) {
				success = insert(key, false);
				if (!success) {
					System.out.println("failed at rejuvenation");
					System.exit(1);
				}
				return true;
			}
		}
		return false;
	}
	
	
	boolean delete(int input) {
		int large_hash = HashFunctions.normal_hash(input);
		int slot_index = get_slot_index(large_hash);
		long fp_long = gen_fingerprint(large_hash);
		//System.out.println("deleting  " + input + "\t b " + slot_index + " \t" + get_fingerprint_str(fp_long, fingerprintLength));
		boolean success = delete(fp_long, slot_index);
		if (success) {
			num_existing_entries--;
			return true;
		}
		
		slot_index = former.get_slot_index(large_hash);
		fp_long = former.gen_fingerprint(large_hash);
		success = former.delete(fp_long, slot_index);
		if (success) {
			num_existing_entries--;
			return true;
		}
		
		for (int i = older_filters.size() - 1; i >= 0; i--) {			
			slot_index = older_filters.get(i).get_slot_index(large_hash);
			fp_long = older_filters.get(i).gen_fingerprint(large_hash);
			success = older_filters.get(i).delete(fp_long, slot_index);
			if (success) {
				return true;
			}
		}
		
		return success; 

	}
	
	double measure_num_bits_per_entry() {
		return measure_num_bits_per_entry(this, new ArrayList<QuotientFilter>(older_filters));
	}

}

