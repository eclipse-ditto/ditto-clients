/*!
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import { Entries, Entry, Policy, Resource, Resources, Subject, Subjects } from '../../model/policies.model';
import { GenericResponse, PutResponse } from '../../model/response';
import { MatchOptions } from '../../options/request.options';
import { RequestSender, RequestSenderFactory } from '../request-factory/request-sender';

export interface PoliciesHandle {
  /**
   * Gets a Policy.
   *
   * @param policyId - The ID of the Policy to get.
   * @param options - Options to use for the request.
   * @returns A Promise for the Policy
   */
  getPolicy(policyId: string, options?: MatchOptions): Promise<Policy>;

  /**
   * Gets the Entries of a Policy.
   *
   * @param policyId - The ID of the Policy.
   * @param options - Options to use for the request.
   * @returns A Promise for the Entries
   */
  getEntries(policyId: string, options?: MatchOptions): Promise<Entries>;

  /**
   * Gets an Entry of a Policy.
   *
   * @param policyId - The ID of the Policy.
   * @param label - The label of the Entry to get.
   * @param options - Options to use for the request.
   * @returns A Promise for the Entry
   */
  getEntry(policyId: string, label: string, options?: MatchOptions): Promise<Entry>;

  /**
   * Gets the Subjects of an Entry.
   *
   * @param policyId - The ID of the Policy the Entry belongs to.
   * @param label - The label of the Entry.
   * @param options - Options to use for the request.
   * @returns A Promise for the Subjects
   */
  getSubjects(policyId: string, label: string, options?: MatchOptions): Promise<Subjects>;

  /**
   * Gets a Subject of an Entry.
   *
   * @param policyId - The ID of the Policy the Entry belongs to.
   * @param label - The label of the Entry.
   * @param subjectId - The ID of the Subject to get.
   * @param options - Options to use for the request.
   * @returns A Promise for the Subject
   */
  getSubject(policyId: string, label: string, subjectId: string, options?: MatchOptions): Promise<Subject>;

  /**
   * Gets the Resources of an Entry.
   *
   * @param policyId - The ID of the Policy the Entry belongs to.
   * @param label - The label of the Entry.
   * @param options - Options to use for the request.
   * @returns A Promise for the Resources
   */
  getResources(policyId: string, label: string, options?: MatchOptions): Promise<Resources>;

  /**
   * Gets a Resource of an Entry.
   *
   * @param policyId - The ID of the Policy the Entry belongs to.
   * @param label - The label of the Entry.
   * @param resourcePath - The path to the Resource.
   * @param options - Options to use for the request.
   * @returns A Promise for the Resource
   */
  getResource(policyId: string, label: string, resourcePath: string, options?: MatchOptions): Promise<Resource>;

  /**
   * Adds or updates a Policy.
   *
   * @param policy - The new Policy.
   * @param options - Options to use for the request.
   * @returns A Promise for a response containing the new Policy if provided by the response
   */
  putPolicy(policy: Policy, options?: MatchOptions): Promise<PutResponse<Policy>>;

  /**
   * Adds or updates the Entries of a Policy.
   *
   * @param policyId - The ID of the Policy.
   * @param entries - The new Entries.
   * @param options - Options to use for the request.
   * @returns A Promise a response containing the new Entries if provided by the response
   */
  putEntries(policyId: string, entries: Entries, options?: MatchOptions): Promise<PutResponse<Entries>>;

  /**
   * Adds or updates an Entry of a Policy.
   *
   * @param policyId - The ID of the Policy.
   * @param entry - The new Entry.
   * @param options - Options to use for the request.
   * @returns A Promise for a response containing the new Entry if provided by the response
   */
  putEntry(policyId: string, entry: Entry, options?: MatchOptions): Promise<PutResponse<Entry>>;

  /**
   * Adds or updates the Subjects of an Entry.
   *
   * @param policyId - The ID of the Policy the Entry belongs to.
   * @param label - The label of the Entry.
   * @param subjects - The new Subjects.
   * @param options - Options to use for the request.
   * @returns A Promise for a response containing the new Subjects if provided by the response
   */
  putSubjects(policyId: string, label: string, subjects: Subjects, options?: MatchOptions): Promise<PutResponse<Subjects>>;

  /**
   * Adds or updates a Subject of an Entry.
   *
   * @param policyId - The ID of the Policy the Entry belongs to.
   * @param label - The label of the Entry.
   * @param subject - The new Subject.
   * @param options - Options to use for the request.
   * @returns A Promise for a response containing the new Subject if provided by the response
   */
  putSubject(policyId: string, label: string, subject: Subject, options?: MatchOptions): Promise<PutResponse<Subject>>;

  /**
   * Adds or updates the Resources of an Entry.
   *
   * @param policyId - The ID of the Policy the Entry belongs to.
   * @param label - The label of the Entry.
   * @param resources - The new Resources.
   * @param options - Options to use for the request.
   * @returns A Promise for a response containing the new Resources if provided by the response
   */
  putResources(policyId: string, label: string, resources: Resources, options?: MatchOptions): Promise<PutResponse<Resources>>;

  /**
   * Adds or updates a Resource of an Entry.
   *
   * @param policyId - The ID of the Policy the Entry belongs to.
   * @param label - The label of the Entry.
   * @param resource - The new Resource.
   * @param options - Options to use for the request.
   * @returns A Promise for a response containing the new Resource if provided by the response
   */
  putResource(policyId: string,
              label: string,
              resource: Resource,
              options?: MatchOptions): Promise<PutResponse<Resource>>;

  /**
   * Deletes a Policy.
   *
   * @param policyId - The ID of the Policy to delete.
   * @param options - Options to use for the request.
   * @returns A Promise for the response
   */
  deletePolicy(policyId: string, options?: MatchOptions): Promise<GenericResponse>;

  /**
   * Deletes an Entry of a Policy.
   *
   * @param policyId - The ID of the Policy.
   * @param label - The label of the Entry to delete.
   * @param options - Options to use for the request.
   * @returns A Promise for the response
   */
  deleteEntry(policyId: string, label: string, options?: MatchOptions): Promise<GenericResponse>;

  /**
   * Deletes a Subject of an Entry.
   *
   * @param policyId - The ID of the Policy the Entry belongs to.
   * @param label - The label of the Entry.
   * @param subjectId - The ID of the Subject to delete.
   * @param options - Options to use for the request.
   * @returns A Promise for the response
   */
  deleteSubject(policyId: string, label: string, subjectId: string, options?: MatchOptions): Promise<GenericResponse>;

  /**
   * Deletes a Subject of an Entry.
   *
   * @param policyId - The ID of the Policy the Entry belongs to.
   * @param label - The label of the Entry.
   * @param resourcePath - The path to the Resource to delete.
   * @param options - Options to use for the request.
   * @returns A Promise for the response
   */
  deleteResource(policyId: string, label: string, resourcePath: string, options?: MatchOptions): Promise<GenericResponse>;
}

/**
 * Handle to send Policies requests.
 */
export class DefaultPoliciesHandle implements PoliciesHandle {

  private constructor(private readonly requestFactory: RequestSender) {
  }

  /**
   * returns an instance of PoliciesHandle using the provided RequestSender.
   *
   * @param builder - The builder for the RequestSender to work with.
   * @returns The PoliciesHandle
   */
  public static getInstance(builder: RequestSenderFactory) {
    return new DefaultPoliciesHandle(builder.buildInstance('policies'));
  }

  /**
   * Gets a Policy.
   *
   * @param policyId - The ID of the Policy to get.
   * @param options - Options to use for the request.
   * @returns A Promise for the Policy
   */
  getPolicy(policyId: string, options?: MatchOptions): Promise<Policy> {
    return this.requestFactory.fetchJsonRequest({
      verb: 'GET',
      parser: o => Policy.fromObject(o, policyId),
      id: policyId,
      requestOptions: options
    });
  }

  /**
   * Gets the Entries of a Policy.
   *
   * @param policyId - The ID of the Policy.
   * @param options - Options to use for the request.
   * @returns A Promise for the Entries
   */
  getEntries(policyId: string, options?: MatchOptions): Promise<Entries> {
    return this.requestFactory.fetchJsonRequest({
      verb: 'GET',
      parser: Entries.fromObject,
      id: policyId,
      path: 'entries',
      requestOptions: options
    });
  }

  /**
   * Gets an Entry of a Policy.
   *
   * @param policyId - The ID of the Policy.
   * @param label - The label of the Entry to get.
   * @param options - Options to use for the request.
   * @returns A Promise for the Entry
   */
  getEntry(policyId: string, label: string, options?: MatchOptions): Promise<Entry> {
    return this.requestFactory.fetchJsonRequest({
      verb: 'GET',
      parser: o => Entry.fromObject(o, label),
      id: policyId,
      path: `entries/${label}`,
      requestOptions: options
    });
  }

  /**
   * Gets the Subjects of an Entry.
   *
   * @param policyId - The ID of the Policy the Entry belongs to.
   * @param label - The label of the Entry.
   * @param options - Options to use for the request.
   * @returns A Promise for the Subjects
   */
  getSubjects(policyId: string, label: string, options?: MatchOptions): Promise<Subjects> {
    return this.requestFactory.fetchJsonRequest({
      verb: 'GET',
      parser: Subjects.fromObject,
      id: policyId,
      path: `entries/${label}/subjects`,
      requestOptions: options
    });
  }

  /**
   * Gets a Subject of an Entry.
   *
   * @param policyId - The ID of the Policy the Entry belongs to.
   * @param label - The label of the Entry.
   * @param subjectId - The ID of the Subject to get.
   * @param options - Options to use for the request.
   * @returns A Promise for the Subject
   */
  getSubject(policyId: string, label: string, subjectId: string, options?: MatchOptions): Promise<Subject> {
    return this.requestFactory.fetchJsonRequest({
      verb: 'GET',
      parser: o => Subject.fromObject(o, subjectId),
      id: policyId,
      path: `entries/${label}/subjects/${subjectId}`,
      requestOptions: options
    });
  }

  /**
   * Gets the Resources of an Entry.
   *
   * @param policyId - The ID of the Policy the Entry belongs to.
   * @param label - The label of the Entry.
   * @param options - Options to use for the request.
   * @returns A Promise for the Resources
   */
  getResources(policyId: string, label: string, options?: MatchOptions): Promise<Resources> {
    return this.requestFactory.fetchJsonRequest({
      verb: 'GET',
      parser: Resources.fromObject,
      id: policyId,
      path: `entries/${label}/resources`,
      requestOptions: options
    });
  }

  /**
   * Gets a Resource of an Entry.
   *
   * @param policyId - The ID of the Policy the Entry belongs to.
   * @param label - The label of the Entry.
   * @param resourcePath - The path to the Resource.
   * @param options - Options to use for the request.
   * @returns A Promise for the Resource
   */
  getResource(policyId: string, label: string, resourcePath: string, options?: MatchOptions): Promise<Resource> {
    return this.requestFactory.fetchJsonRequest({
      verb: 'GET',
      parser: o => Resource.fromObject(o, resourcePath),
      id: policyId,
      path: `entries/${label}/resources/${resourcePath}`,
      requestOptions: options
    });
  }

  /**
   * Adds or updates a Policy.
   *
   * @param policy - The new Policy.
   * @param options - Options to use for the request.
   * @returns A Promise for a response containing the new Policy if provided by the response
   */
  putPolicy(policy: Policy, options?: MatchOptions): Promise<PutResponse<Policy>> {
    return this.requestFactory.fetchPutRequest({
      verb: 'PUT',
      parser: o => Policy.fromObject(o, policy.id),
      id: policy.id,
      requestOptions: options,
      payload: policy.toObject()
    });
  }

  /**
   * Adds or updates the Entries of a Policy.
   *
   * @param policyId - The ID of the Policy.
   * @param entries - The new Entries.
   * @param options - Options to use for the request.
   * @returns A Promise a response containing the new Entries if provided by the response
   */
  putEntries(policyId: string, entries: Entries, options?: MatchOptions): Promise<PutResponse<Entries>> {
    return this.requestFactory.fetchPutRequest({
      verb: 'PUT',
      parser: Entries.fromObject,
      id: policyId,
      path: 'entries',
      requestOptions: options,
      payload: Entries.toObject(entries)
    });
  }

  /**
   * Adds or updates an Entry of a Policy.
   *
   * @param policyId - The ID of the Policy.
   * @param entry - The new Entry.
   * @param options - Options to use for the request.
   * @returns A Promise for a response containing the new Entry if provided by the response
   */
  putEntry(policyId: string, entry: Entry, options?: MatchOptions): Promise<PutResponse<Entry>> {
    return this.requestFactory.fetchPutRequest({
      verb: 'PUT',
      parser: o => Entry.fromObject(o, entry.id),
      id: policyId,
      path: `entries/${entry.id}`,
      requestOptions: options,
      payload: entry.toObject()
    });
  }

  /**
   * Adds or updates the Subjects of an Entry.
   *
   * @param policyId - The ID of the Policy the Entry belongs to.
   * @param label - The label of the Entry.
   * @param subjects - The new Subjects.
   * @param options - Options to use for the request.
   * @returns A Promise for a response containing the new Subjects if provided by the response
   */
  putSubjects(policyId: string, label: string, subjects: Subjects, options?: MatchOptions): Promise<PutResponse<Subjects>> {
    return this.requestFactory.fetchPutRequest({
      verb: 'PUT',
      parser: Subjects.fromObject,
      id: policyId,
      path: `entries/${label}/subjects`,
      requestOptions: options,
      payload: Subjects.toObject(subjects)
    });
  }

  /**
   * Adds or updates a Subject of an Entry.
   *
   * @param policyId - The ID of the Policy the Entry belongs to.
   * @param label - The label of the Entry.
   * @param subject - The new Subject.
   * @param options - Options to use for the request.
   * @returns A Promise for a response containing the new Subject if provided by the response
   */
  putSubject(policyId: string, label: string, subject: Subject, options?: MatchOptions): Promise<PutResponse<Subject>> {
    return this.requestFactory.fetchPutRequest({
      verb: 'PUT',
      parser: o => Subject.fromObject(o, subject.id),
      id: policyId,
      path: `entries/${label}/subjects/${subject.id}`,
      requestOptions: options,
      payload: subject.toObject()
    });
  }

  /**
   * Adds or updates the Resources of an Entry.
   *
   * @param policyId - The ID of the Policy the Entry belongs to.
   * @param label - The label of the Entry.
   * @param resources - The new Resources.
   * @param options - Options to use for the request.
   * @returns A Promise for a response containing the new Resources if provided by the response
   */
  putResources(policyId: string, label: string, resources: Resources, options?: MatchOptions): Promise<PutResponse<Resources>> {
    return this.requestFactory.fetchPutRequest({
      verb: 'PUT',
      parser: Resources.fromObject,
      id: policyId,
      path: `entries/${label}/resources`,
      requestOptions: options,
      payload: Resources.toObject(resources)
    });
  }

  /**
   * Adds or updates a Resource of an Entry.
   *
   * @param policyId - The ID of the Policy the Entry belongs to.
   * @param label - The label of the Entry.
   * @param resource - The new Resource.
   * @param options - Options to use for the request.
   * @returns A Promise for a response containing the new Resource if provided by the response
   */
  putResource(policyId: string,
              label: string,
              resource: Resource,
              options?: MatchOptions): Promise<PutResponse<Resource>> {
    return this.requestFactory.fetchPutRequest({
      verb: 'PUT',
      parser: o => Resource.fromObject(o, resource.id),
      id: policyId,
      path: `entries/${label}/resources/${resource.id}`,
      requestOptions: options,
      payload: resource.toObject()
    });
  }

  /**
   * Deletes a Policy.
   *
   * @param policyId - The ID of the Policy to delete.
   * @param options - Options to use for the request.
   * @returns A Promise for the response
   */
  deletePolicy(policyId: string, options?: MatchOptions): Promise<GenericResponse> {
    return this.requestFactory.fetchRequest({
      verb: 'DELETE',
      id: policyId,
      requestOptions: options
    });
  }

  /**
   * Deletes an Entry of a Policy.
   *
   * @param policyId - The ID of the Policy.
   * @param label - The label of the Entry to delete.
   * @param options - Options to use for the request.
   * @returns A Promise for the response
   */
  deleteEntry(policyId: string, label: string, options?: MatchOptions): Promise<GenericResponse> {
    return this.requestFactory.fetchRequest({
      verb: 'DELETE',
      id: policyId,
      path: `entries/${label}`,
      requestOptions: options
    });
  }

  /**
   * Deletes a Subject of an Entry.
   *
   * @param policyId - The ID of the Policy the Entry belongs to.
   * @param label - The label of the Entry.
   * @param subjectId - The ID of the Subject to delete.
   * @param options - Options to use for the request.
   * @returns A Promise for the response
   */
  deleteSubject(policyId: string, label: string, subjectId: string, options?: MatchOptions): Promise<GenericResponse> {
    return this.requestFactory.fetchRequest({
      verb: 'DELETE',
      id: policyId,
      path: `entries/${label}/subjects/${subjectId}`,
      requestOptions: options
    });
  }

  /**
   * Deletes a Subject of an Entry.
   *
   * @param policyId - The ID of the Policy the Entry belongs to.
   * @param label - The label of the Entry.
   * @param resourcePath - The path to the Resource to delete.
   * @param options - Options to use for the request.
   * @returns A Promise for the response
   */
  deleteResource(policyId: string, label: string, resourcePath: string, options?: MatchOptions): Promise<GenericResponse> {
    return this.requestFactory.fetchRequest({
      verb: 'DELETE',
      id: policyId,
      path: `entries/${label}/resources/${resourcePath}`,
      requestOptions: options
    });
  }
}
