package me.yang.recsys.wikivoyage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import me.yang.recsys.wikivoyage.docbase.DocBase;
import me.yang.recsys.wikivoyage.models.GeneralVectorModel;
import me.yang.recsys.wikivoyage.rec.RecForSearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Boot the program
 */
public class Boot {
    private static final Logger logger = LoggerFactory.getLogger(Boot.class);

    public static void main(String[] args) {
        String fasttextModelFile = args[0]; // word2vec model trained by fasttext
        String wikivoyageDocBaseFile = args[1]; // preprocessed listings docbase file
        String dataEngineerDatasetFile = args[2]; // user-searchedterm file
        String resultFile = args[3]; // user-searchedterm-recommendterms file

        // Initialize models
        GeneralVectorModel generalVectorModel = new GeneralVectorModel(fasttextModelFile, 128, " ");
        DocBase.loadDocBase(wikivoyageDocBaseFile);
        RecForSearch.setGeneralVectorModel(generalVectorModel);

        // Read searched term from dataEngineerDataset
        // recommend 3 POIs and save local
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultFile), "utf-8"));
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(dataEngineerDatasetFile), "utf-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line.trim());
                int firstComma = line.trim().indexOf(",");
                String searchedTerm = line.trim().substring(firstComma + 1);
                if (searchedTerm.startsWith("\"") && searchedTerm.endsWith("\""))
                    searchedTerm = searchedTerm.substring(1, searchedTerm.length()-1);
                List<String> candidates = RecForSearch.recommend(searchedTerm);
                for (String candidate: candidates) {
                    writer.write(","+candidate);
                }
                writer.write("\n");
            }
            reader.close();
            writer.close();
        } catch (Exception e) {
            logger.error("", e);
        }
    }

}
