/*
 * Copyright 2016 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.eva.pipeline.parameters.validation.step;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;

import uk.ac.ebi.eva.pipeline.configuration.jobs.steps.LoadVariantsStepConfiguration;
import uk.ac.ebi.eva.pipeline.parameters.JobParametersNames;
import uk.ac.ebi.eva.test.rules.PipelineTemporaryFolderRule;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Tests that the arguments necessary to run a {@link LoadVariantsStepConfiguration} are correctly
 * validated
 */
public class LoadVariantsStepParametersValidatorTest {

    private JobParametersValidator validator;

    @Rule
    public PipelineTemporaryFolderRule temporaryFolderRule = new PipelineTemporaryFolderRule();

    private Map<String, JobParameter> requiredParameters;

    private Map<String, JobParameter> optionalParameters;

    @Before
    public void setUp() throws IOException {
        validator = new LoadVariantsStepParametersValidator();

        requiredParameters = new TreeMap<>();
        requiredParameters.put(JobParametersNames.DB_NAME, new JobParameter("database"));
        requiredParameters.put(JobParametersNames.DB_COLLECTIONS_VARIANTS_NAME, new JobParameter("variants"));
        requiredParameters.put(JobParametersNames.INPUT_STUDY_ID, new JobParameter("inputStudyId"));
        requiredParameters.put(JobParametersNames.INPUT_VCF_ID, new JobParameter("inputVcfId"));
        requiredParameters.put(JobParametersNames.INPUT_VCF_AGGREGATION, new JobParameter("NONE"));
        requiredParameters.put(JobParametersNames.INPUT_VCF,
                               new JobParameter(temporaryFolderRule.newFile().getCanonicalPath()));
        requiredParameters.put(JobParametersNames.INPUT_VCF_AGGREGATION_MAPPING_PATH,
                               new JobParameter(temporaryFolderRule.newFile().getCanonicalPath()));

        optionalParameters = new TreeMap<>();
        optionalParameters.put(JobParametersNames.CONFIG_CHUNK_SIZE, new JobParameter("100"));
        optionalParameters.put(JobParametersNames.CONFIG_RESTARTABILITY_ALLOW, new JobParameter("true"));
    }

    @Test
    public void allJobParametersAreValid() throws JobParametersInvalidException, IOException {
        validator.validate(new JobParameters(requiredParameters));
    }

    @Test
    public void allJobParametersIncludingOptionalAreValid() throws JobParametersInvalidException, IOException {
        Map<String, JobParameter> parameters = new TreeMap<>();
        parameters.putAll(requiredParameters);
        parameters.putAll(optionalParameters);
        validator.validate(new JobParameters(parameters));
    }

    @Test(expected = JobParametersInvalidException.class)
    public void dbNameIsRequired() throws Exception {
        requiredParameters.remove(JobParametersNames.DB_NAME);
        validator.validate(new JobParameters(requiredParameters));
    }

    @Test(expected = JobParametersInvalidException.class)
    public void dbCollectionsVariantsNameIsRequired() throws Exception {
        requiredParameters.remove(JobParametersNames.DB_COLLECTIONS_VARIANTS_NAME);
        validator.validate(new JobParameters(requiredParameters));
    }

    @Test(expected = JobParametersInvalidException.class)
    public void inputStudyIdIsRequired() throws Exception {
        requiredParameters.remove(JobParametersNames.INPUT_STUDY_ID);
        validator.validate(new JobParameters(requiredParameters));
    }

    @Test(expected = JobParametersInvalidException.class)
    public void inputVcfIdIsRequired() throws Exception {
        requiredParameters.remove(JobParametersNames.INPUT_VCF_ID);
        validator.validate(new JobParameters(requiredParameters));
    }

    @Test(expected = JobParametersInvalidException.class)
    public void inputVcfIsRequired() throws Exception {
        requiredParameters.remove(JobParametersNames.INPUT_VCF);
        validator.validate(new JobParameters(requiredParameters));
    }

    @Test(expected = JobParametersInvalidException.class)
    public void inputVcfAggregationIsRequired() throws Exception {
        requiredParameters.remove(JobParametersNames.INPUT_VCF_AGGREGATION);
        validator.validate(new JobParameters(requiredParameters));
    }

}
