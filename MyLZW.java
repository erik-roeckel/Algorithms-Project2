/*************************************************************************
 *  Compilation:  javac LZW.java
 *  Execution:    java LZW - < input.txt   (compress)
 *  Execution:    java LZW + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *
 *  Compress or expand binary input from standard input using LZW.
 *
 *  WARNING: STARTING WITH ORACLE JAVA 6, UPDATE 7 the SUBSTRING
 *  METHOD TAKES TIME AND SPACE LINEAR IN THE SIZE OF THE EXTRACTED
 *  SUBSTRING (INSTEAD OF CONSTANT SPACE AND TIME AS IN EARLIER
 *  IMPLEMENTATIONS).
 *
 *  See <a href = "http://java-performance.info/changes-to-string-java-1-7-0_06/">this article</a>
 *  for more details.
 *
 *************************************************************************/
// Erik Roeckel
// MyLZW.java
/***********************************************************************/
public class MyLZW {
    private static final int R = 256;        // number of input chars
    private static int L = 512;       // number of codewords = 2^W
    private static int W = 9;       // codeword start width

    public static void compress(char mode) {
        //BinaryStdIn throws excpetion is # of bits available on standard input is not multiple of 8 bits
        int uncompressedSize = 0;
        int compressedSize = 0;
        double oldRatio = 0;
        double newRatio = 0;
        boolean keepReading = true; // boolean variable for input.length() > 0
        String input = BinaryStdIn.readString(); // reads in the remaining bytes of data from standard input and return it as a string
        
        TST<Integer> st = new TST<Integer>(); // creates new TST instance
        for (int i = 0; i < R; i++)
            st.put("" + (char) i, i); // inserts each ASCII value int TST
        int code = R+1;  // R is codeword for EOF --> this means we are at the last codeword, the file starts at 257
        
        //loops until there is nothing left in the file
        while (keepReading == true) {
            String s = st.longestPrefixOf(input);  //Current character or current longest matching prefix stored in codebook
            BinaryStdOut.write(st.get(s), W); // converts it to sequences of 9 bit codewords
            int t = s.length(); // stores length of string stored in curr, which will be longest matcing prefix found in codebook
            uncompressedSize += (t * 8); // multiplying # of chars in string by 8 bits
            compressedSize += W; // gives size of compressed file == to the amount of codewords generated 
            if (t >= input.length())
                keepReading = false;
        
            if (keepReading == true && code < L)    // Add s to symbol table.
            {
                st.put(input.substring(0, t + 1), code++); //this inserts a key value pair into the TST of the codeword to add
                oldRatio = ((double)uncompressedSize/(double)compressedSize); // gives compression ratio of when codebook was last filled
            }
            else if(keepReading == true && code >= L && W < 16)
            {
                L *= 2;
                W++;
                st.put(input.substring(0, t + 1), code++);

            }
            else if(keepReading == true && code == L && W == 16)
            {
                boolean monitorMode = false; // boolean variable to establish difference between monitor and reset mode
                switch(mode)
                {
                    case 'n': // do nothing mode
                    {
                        break;
                    }
                    case 'm': //monitor mode, which then uses reset mode if compression ratio is 1.1 or more
                    {
                        mode = 'r';
                        monitorMode = true;
                    }
                    case 'r': //combined monitor and reset mode to eliminate code redundancy
                    {
                        double compressionRatio = 0;
                        if(monitorMode == true) // enters if user requested monitor mode
                        {
                            mode = 'm';
                            newRatio = ((double)uncompressedSize/(double)compressedSize); // gives ratio of current compression
                            compressionRatio = (oldRatio/newRatio); // calculates ratio of oldRatio: newRatio
                        }

                        if(mode == 'r' || compressionRatio > 1.1) 
                        {
                            st = new TST<Integer>();
                            for (int i = 0; i < R; i++)
                                st.put("" + (char) i, i);

                            W = 9; // sets width of codewords back to 9
                            L = 512; // sets size of codebook back to original 2^9
                            code = R + 1; // reset code so we add the next code to codebook 257
                            st.put(input.substring(0, t + 1), code++);
                            
                        }
                        break;
                    }

                }
                
            }
            input = input.substring(t); // Scan past s in input.
        }
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    } 


    public static void expand() {
        String[] st = new String[65536]; // uses a instance of perfect hashing, mapping codewords to indices
        int i; // next available codeword value
        char modeSelect = BinaryStdIn.readChar();
        int compressedSize = 0;
        int uncompressedSize = 0;
        double oldRatio = 0;
        double newRatio = 0;
        // initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++)
            st[i] = "" + (char) i;// this reads in every ASCII char even if it isn't in file
        st[i++] = "";  // (unused) lookahead for EOF

        int codeword = BinaryStdIn.readInt(W); // this handles the first output letter which has no add
        if (codeword == R) return;           // expanded message is empty string
        String val = st[codeword];

        while (true)
        {
            int t = val.length();
            compressedSize += W; // gives size of compressed file == to the amount of codewords generated 
            uncompressedSize += (t * 8); // multiplying # of chars in string by 8 bits
            BinaryStdOut.write(val);

            if(i >= L && W < 16) //executes if our codebook is full and we have room to add more codewords
            {
                L *= 2; //Increase size of codebook by power of 2
                W++; //Increment size of codewords
                oldRatio = ((double)uncompressedSize/(double)compressedSize); // gives compression ratio of when codebook was last filled
            }
            else if(i >= L && W == 16)
            {
                boolean monitorMode = false; //boolean variable for confirming user requested monitor mode
                switch(modeSelect)
                {
                    case 'n': //case for do nothing mode
                    {
                        break;
                    }
                    case 'm': // case for monitor mode, which uses reset mode
                    {
                        modeSelect = 'r'; // this way monitor mode will be able to use reset modes codebook reset
                        monitorMode = true; // the user has entered 'm' as there mode
                    }
                    case 'r': // case for resetting codebook , combined monitor and reset mode to eliminate code redundancy
                    {
                        double expansionRatio = 0;
                        if(monitorMode == true)
                        {
                            modeSelect ='m';
                            newRatio = ((double)uncompressedSize/(double)compressedSize); // gives ratio of current compression
                            expansionRatio = (oldRatio/newRatio); //calculates ratio of oldRatio: newRatio
                        }
                        if(modeSelect == 'r'|| expansionRatio > 1.1)
                        {
                            if(expansionRatio > 1.1) // resets oldRatio is our expansion ration > 1.1
                                oldRatio = 0;
                                
                            int codewordVal;
                            st = new String[65536]; // instantiates new codebook to fill
                            for(codewordVal = 0; codewordVal < R; codewordVal++)
                                st[codewordVal] = "" + (char) codewordVal; // this reads in every ASCII char even if it isn't in file
                            st[codewordVal++] = "";
                            W = 9; // resets codeword width to 9 bits
                            L = 512; // resets codebook size to 2^9
                            i = R + 1;
                        }
                        break;
                    }
                }
            }
                codeword = BinaryStdIn.readInt(W);
                if (codeword == R) break;
                String s = st[codeword];
                if (i == codeword) s = val + val.charAt(0); // special case hack
                if (i < L)
                {
                    st[i++] = val + s.charAt(0);
                    oldRatio = ((double)uncompressedSize/(double)compressedSize); // updates compression ratio in the case that the codebook did not fill completley
                }
                val = s;
        }
        BinaryStdOut.close();
    }



    public static void main(String[] args) {
        char mode;
        if(args[0].equals("-"))
        {
            mode = args[1].charAt(0);
            if(mode == 'n')
                BinaryStdOut.write(mode); //so expansion automatically knows user picked do nothing mode
            else if(mode == 'r')
                BinaryStdOut.write(mode); //so expansion automatically knows user picked reset mode
            else if(mode == 'm')
                BinaryStdOut.write(mode); //so expansion automatically knows user picked monitor mode
            compress(mode);
        }
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }

}