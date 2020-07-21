package bu.COVIDApp.Auxiliary;

import bu.COVIDApp.Database.SQLBloomFilter.SQLBloomFilterData;
import bu.COVIDApp.RestService.InfectedKeyUpload.InfectedKey;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class BloomFilter {

    //TODO: Might be better to set these parameters at launch time rather than hard coding
    //Should do some math to determine good values for these parameters
    private final int HASH_CEILING = 2048;
    private final int NUM_HASHES = 3;
    private final int BYTE_SIZE = 8;

    //Might be better to store only non-zero indices
    private byte[] filterData;

    public BloomFilter(){
        this.filterData = new byte[HASH_CEILING];
    }

    public BloomFilter(byte[] myData){
       this.filterData = myData;
    }

    public BloomFilter(List<InfectedKey> myData){
        this.filterData = new byte[HASH_CEILING];
        this.insert(myData);
    }

    public BloomFilter(ArrayList<SQLBloomFilterData> myData){
        this.filterData = new byte[HASH_CEILING];

        //Put every item you get back in its correct place in the bloom filter
        //If an index is not found in the database it is assumed to be zero
        for(SQLBloomFilterData key:myData)
            this.filterData[key.getIndex()] = key.getData();
    }

    /**
     * Insert a list of keys into the bloomFilter
     * @param myData The data you want to insert into your bloom filter
     * @return A HashSet of the INDICES that have been set
     */
    public HashSet<Integer> insert(List<InfectedKey> myData){
        HashSet<Integer> returnSet = new HashSet<>();

        for(InfectedKey key:myData){
            for(int ii = 0; ii < NUM_HASHES;ii++){
                int hash = keyHash(key.getChirp(),ii);
                byte beforeChanges = filterData[hash/BYTE_SIZE];
                //Index into the group of 8 bits that you need to update. Set the relevant bit in that group
                byte mask = (byte)(BYTE_SIZE - 1 - (hash%BYTE_SIZE));
                filterData[hash/BYTE_SIZE] = (byte)(filterData[hash/BYTE_SIZE] | (1 << mask));

                if(filterData[hash/BYTE_SIZE] != beforeChanges)
                    returnSet.add(hash);
            }
        }

        return returnSet;
    }

    /**
     * As this is a bloom filter, there is a probability
     * @param myData the list of keys you want to check
     * @return true if a match is found, false otherwise
     */
    public boolean findMatches(List<InfectedKey> myData){
        for(InfectedKey key:myData){
            boolean found = true;
            for(int ii = 0; ii < NUM_HASHES;ii++){
                int hash = keyHash(key.getChirp(),ii);
                //Index into the group of 8 bits that you need to update. Set the relevant bit in that group
                byte mask = (byte)(BYTE_SIZE - 1 - (hash%BYTE_SIZE));

                //If you OR with the relevant bit and the number changes, the bit was not set
                if(filterData[hash/BYTE_SIZE] != (byte)(filterData[hash/BYTE_SIZE] | (1 << mask))){
                    found = false;
                    break;
                }
            }
            if(found)
                return true;
        }

        return false;
    }


    public byte[] getFilterData(){
        return filterData;
    }

    /**
     * TODO: Put an actual hash function here that can be performed on the frontend as well(maybe murmur3)
     * @param inputKey the key that you would like to get the hash value of
     * @param modifier an integer that can be adjusted to give a second hash for the same input
     * @return an integer hash of your input
     */
    private Integer keyHash(String inputKey,int modifier){
        return Math.abs(inputKey.hashCode() * 51 * modifier) % HASH_CEILING;
    }}