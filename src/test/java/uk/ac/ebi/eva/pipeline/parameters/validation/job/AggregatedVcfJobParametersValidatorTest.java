/*
 * Copyright 2017 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.eva.pipeline.parameters.validation.job;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;

import uk.ac.ebi.eva.pipeline.parameters.JobParametersNames;
import uk.ac.ebi.eva.test.rules.PipelineTemporaryFolderRule;

import java.util.Map;
import java.util.TreeMap;

/**
 * Tests that the arguments necessary to run a {@link uk.ac.ebi.eva.pipeline.jobs.AggregatedVcfJob} are
 * correctly validated
 */
public class AggregatedVcfJobParametersValidatorTest {

    private AggregatedVcfJobParametersValidator validator;

    @Rule
    public PipelineTemporaryFolderRule temporaryFolder = new PipelineTemporaryFolderRule();

    private Map<String, JobParameter> requiredParameters;

    private Map<String, JobParameter> annotationParameters;

    private Map<String, JobParameter> optionalParameters;

    @Before
    public void setUp() throws Exception {
        validator = new AggregatedVcfJobParametersValidator();
        final String dir = temporaryFolder.getRoot().getCanonicalPath();

        requiredParameters = new TreeMap<>();

        // variant load step
        requiredParameters.put(JobParametersNames.DB_NAME, new JobParameter("database"));
        requiredParameters.put(JobParametersNames.DB_COLLECTIONS_VARIANTS_NAME, new JobParameter("variants"));
        requiredParameters.put(JobParametersNames.INPUT_STUDY_ID, new JobParameter("inputStudyId"));
        requiredParameters.put(JobParametersNames.INPUT_VCF_ID, new JobParameter("inputVcfId"));
        requiredParameters.put(JobParametersNames.INPUT_VCF_AGGREGATION, new JobParameter("NONE"));
        requiredParameters.put(JobParametersNames.INPUT_VCF,
                new JobParameter(temporaryFolder.newFile().getCanonicalPath()));

        // file load step
        requiredParameters.put(JobParametersNames.DB_COLLECTIONS_FILES_NAME, new JobParameter("collectionsFilesName"));
        requiredParameters.put(JobParametersNames.INPUT_STUDY_NAME, new JobParameter("inputStudyName"));
        requiredParameters.put(JobParametersNames.INPUT_STUDY_TYPE, new JobParameter("COLLECTION"));

        // annotation
        requiredParameters.put(JobParametersNames.ANNOTATION_SKIP, new JobParameter("true"));

        annotationParameters = new TreeMap<>();
        annotationParameters.put(JobParametersNames.OUTPUT_DIR_ANNOTATION, new JobParameter(dir));
        annotationParameters.put(JobParametersNames.APP_VEP_CACHE_SPECIES, new JobParameter("Human"));
        annotationParameters.put(JobParametersNames.APP_VEP_CACHE_VERSION, new JobParameter("100_A"));
        annotationParameters.put(JobParametersNames.APP_VEP_NUMFORKS, new JobParameter("6"));
        annotationParameters.put(JobParametersNames.APP_VEP_CACHE_PATH,
                new JobParameter(temporaryFolder.getRoot().getCanonicalPath()));
        annotationParameters.put(JobParametersNames.APP_VEP_PATH,
                new JobParameter(temporaryFolder.newFile().getCanonicalPath()));
        annotationParameters.put(JobParametersNames.INPUT_FASTA,
                new JobParameter(temporaryFolder.newFile().getCanonicalPath()));


        // optionals
        optionalParameters = new TreeMap<>();
        optionalParameters.put(JobParametersNames.CONFIG_CHUNK_SIZE, new JobParameter("100"));
        optionalParameters.put(JobParametersNames.CONFIG_RESTARTABILITY_ALLOW, new JobParameter("true"));
    }

    // The next tests show behaviour about the required parameters

    @Test
    public void allJobParametersAreValid() throws JobParametersInvalidException {
        Map<String, JobParameter> parameters = new TreeMap<>();
        parameters.putAll(requiredParameters);
        parameters.putAll(annotationParameters);
        parameters.putAll(optionalParameters);
        validator.validate(new JobParameters(parameters));
    }

    @Test(expected = JobParametersInvalidException.class)
    public void dbNameIsRequiredSkippingAnnotation() throws JobParametersInvalidException {
        Map<String, JobParameter> parameters = new TreeMap<>();
        parameters.putAll(requiredParameters);
        parameters.putAll(optionalParameters);
        parameters.remove(JobParametersNames.DB_NAME);
        validator.validate(new JobParameters(parameters));
    }

    @Test(expected = JobParametersInvalidException.class)
    public void dbNameIsRequiredWithoutSkippingAnnotation() throws JobParametersInvalidException {
        Map<String, JobParameter> parameters = new TreeMap<>();
        parameters.putAll(requiredParameters);
        parameters.putAll(optionalParameters);
        parameters.putAll(annotationParameters);
        parameters.put(JobParametersNames.ANNOTATION_SKIP, new JobParameter("false"));
        parameters.remove(JobParametersNames.DB_NAME);
        validator.validate(new JobParameters(parameters));
    }

    // The next tests show what happens when not all the annotation parameters are present

    @Test
    public void annotationParametersAreNotRequiredIfAnnotationIsSkipped() throws JobParametersInvalidException {
        Map<String, JobParameter> parameters = new TreeMap<>();
        parameters.putAll(requiredParameters);
        parameters.putAll(optionalParameters);
        validator.validate(new JobParameters(parameters));
    }

    @Test(expected = JobParametersInvalidException.class)
    public void annotationParametersAreRequiredIfAnnotationIsNotSkipped() throws JobParametersInvalidException {
        Map<String, JobParameter> parameters = new TreeMap<>();
        parameters.putAll(requiredParameters);
        parameters.putAll(optionalParameters);
        parameters.put(JobParametersNames.ANNOTATION_SKIP, new JobParameter("false"));
        validator.validate(new JobParameters(parameters));
    }

    /**
     * The parameters APP_VEP_CACHE_SPECIES is chosen as one belonging to the annotation parameters. We don't check
     * for every annotation parameter, because in AnnotationLoaderStepParametersValidatorTest,
     * VepAnnotationGeneratorStepParametersValidatorTest and VepInputGeneratorStepParametersValidatorTest, it is already
     * checked that every missing required parameter makes the validation fail.
     */
    @Test(expected = JobParametersInvalidException.class)
    public void appVepCacheSpeciesIsRequiredIfAnnotationIsNotSkipped() throws JobParametersInvalidException {
        Map<String, JobParameter> parameters = new TreeMap<>();
        parameters.putAll(requiredParameters);
        parameters.putAll(annotationParameters);
        parameters.putAll(optionalParameters);
        parameters.remove(JobParametersNames.APP_VEP_CACHE_SPECIES);
        parameters.put(JobParametersNames.ANNOTATION_SKIP, new JobParameter("false"));
        validator.validate(new JobParameters(parameters));
    }
}
