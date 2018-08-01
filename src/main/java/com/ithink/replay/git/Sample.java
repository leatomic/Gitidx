package com.ithink.replay.git;

import com.ithink.replay.git.model.IndexState;

import java.io.IOException;

public class Sample {

    public static void main(String[] args) throws IOException {

        IndexStateResolver resolver = new IndexStateResolver();

        String source = "D:\\version-control\\index-follow\\.git\\index";
        IndexState index = resolver.resolve(source);

        index.display();

        String target = "D:\\version-control\\index-follow_index.view";
        index.dump(target);

    }

}
